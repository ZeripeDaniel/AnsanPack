package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.MissionManager;
import com.ansan.ansanpack.mission.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.ItemFishedEvent;

import java.sql.Connection;
import java.util.List;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID)
public class MissionEventHandler {

    @SubscribeEvent
    public static void onPlayerEat(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (player.level().isClientSide) return;
        if (!stack.isEdible()) return;

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"eat".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);

            boolean match = conditions.isEmpty(); // 조건 없으면 전부 허용
            for (MissionCondition cond : conditions) {
                if ("item_id".equals(cond.key) && "eq".equals(cond.comparison)) {
                    String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                    if (itemId.equals(cond.value)) {
                        match = true;
                        break;
                    }
                }
            }

            if (match) {
                mission.progress++;
                if (mission.progress >= def.goalValue) {
                    mission.completed = true;
                }

                try (Connection conn = MissionDB.getConnection()) {
                    PlayerMissionDAO.saveOrUpdatePlayerMission(conn, mission);
                } catch (Exception e) {
                    AnsanPack.LOGGER.error("미션 진행도 저장 실패", e);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onFishing(ItemFishedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        List<ItemStack> drops = event.getDrops(); // 낚은 아이템들
        if (drops.isEmpty()) return;

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"fishing".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);

            boolean match = conditions.isEmpty(); // 조건 없으면 통과
            for (MissionCondition cond : conditions) {
                if ("item_id".equals(cond.key) && "eq".equals(cond.comparison)) {
                    match = false;
                    for (ItemStack drop : drops) {
                        String itemId = ForgeRegistries.ITEMS.getKey(drop.getItem()).toString();
                        if (itemId.equals(cond.value)) {
                            match = true;
                            break;
                        }
                    }
                }
            }

            if (match) {
                mission.progress++;
                if (mission.progress >= def.goalValue) {
                    mission.completed = true;
                }

                try (Connection conn = MissionDB.getConnection()) {
                    PlayerMissionDAO.saveOrUpdatePlayerMission(conn, mission);
                } catch (Exception e) {
                    AnsanPack.LOGGER.error("낚시 미션 저장 실패", e);
                }
            }
        }
    }

}
