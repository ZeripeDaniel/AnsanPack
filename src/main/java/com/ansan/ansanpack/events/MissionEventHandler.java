package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.MissionManager;
import com.ansan.ansanpack.mission.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.level.BlockEvent;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID)
public class MissionEventHandler {

    // 이동 미션은 클라이언트 측에서 추적하고 /미션 화면에서만 서버에 전송함
    //private static final Map<String, net.minecraft.world.phys.Vec3> lastPositions = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerEat(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (player.level().isClientSide) return;
        if (!stack.isEdible()) return;

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            if (mission.completed) continue; // 여기를 수정했음: 완료된 미션은 무시

            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"eat".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty();

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

        List<ItemStack> drops = event.getDrops();
        if (drops.isEmpty()) return;

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            if (mission.completed) continue; // 여기를 수정했음: 완료된 미션은 무시

            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"fishing".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty();

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

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        MissionEventDispatcher.onKillEntity(player, event.getEntity().getType());
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) return;

        MissionEventDispatcher.onItemCrafted(player, crafted);
    }

    @SubscribeEvent
    public static void onItemCooked(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        ItemStack result = event.getSmelting();
        if (result.isEmpty()) return;

        MissionEventDispatcher.onItemCooked(player, result);
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        String blockId = ForgeRegistries.BLOCKS.getKey(event.getState().getBlock()).toString();
        if (blockId == null) return;

        MissionEventDispatcher.onBlockMined(player, blockId);
    }
// 이동 미션은 클라이언트 측에서 추적하고 /미션 화면에서만 서버에 전송함

//    @SubscribeEvent
//    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
//        if (!(event.player instanceof ServerPlayer player)) return;
//        if (player.level().isClientSide) return;
//
//        String uuid = player.getStringUUID();
//        net.minecraft.world.phys.Vec3 current = player.position();
//        net.minecraft.world.phys.Vec3 last = lastPositions.get(uuid);
//
//        if (last != null) {
//            double dx = current.x - last.x;
//            double dy = current.y - last.y;
//            double dz = current.z - last.z;
//            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
//
//            if (dist > 0.05) {
//                MissionEventDispatcher.onPlayerMoved(player, dist);
//            }
//        }
//
//        lastPositions.put(uuid, current);
//    }

    @SubscribeEvent
    public static void onPlayerWakeUp(net.minecraftforge.event.entity.player.PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        if (!event.updateLevel()) return;

        MissionEventDispatcher.onPlayerSlept(player);
    }
}
