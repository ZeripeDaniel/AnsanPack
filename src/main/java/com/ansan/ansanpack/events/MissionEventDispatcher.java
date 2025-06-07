package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.MissionManager;
import com.ansan.ansanpack.mission.*;

import java.sql.Connection;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.common.ForgeHooks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.PickaxeItem;

public class MissionEventDispatcher {

    public static void onUpgradeAttempt(ServerPlayer player, boolean success) {
        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            if (mission.completed) continue; // 여기를 수정했음: 완료된 미션 무시

            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"upgrade".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty();

            for (MissionCondition cond : conditions) {
                if ("result".equals(cond.key)) {
                    if ("success".equals(cond.value) && "eq".equals(cond.comparison) && !success) match = false;
                    if ("fail".equals(cond.value) && "eq".equals(cond.comparison) && success) match = false;
                }
            }

           // logProgress(player, mission, def, "upgrade", String.valueOf(match));

            if (match) {
                mission.progress++;
                if (mission.progress >= def.goalValue) {
                    mission.completed = true;
                }

                try (Connection conn = MissionDB.getConnection()) {
                    PlayerMissionDAO.saveOrUpdatePlayerMission(conn, mission);
                } catch (Exception e) {
                    AnsanPack.LOGGER.error("강화 미션 저장 실패", e);
                }
            }
        }
    }

    public static void onKillEntity(ServerPlayer player, EntityType<?> killedEntityType) {
        String killedEntityId = ForgeRegistries.ENTITY_TYPES.getKey(killedEntityType).toString();
        //AnsanPack.LOGGER.warn("onKillEntity 호출됨: {}", killedEntityId);

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());
        //AnsanPack.LOGGER.warn("플레이어 미션 수: {}", missions.size());

        for (PlayerMissionData mission : missions) {
            if (mission.completed) continue;

            MissionData def = MissionManager.getMission(mission.missionId);
           // AnsanPack.LOGGER.warn("미션 ID={}, 타입={}", mission.missionId, def != null ? def.goalType : "null");
            if (def == null || !"kill_entity".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty();
           // AnsanPack.LOGGER.warn("조건 수: {}", conditions.size());

            for (MissionCondition cond : conditions) {
                //AnsanPack.LOGGER.warn("조건 key={}, comparison={}, value={}", cond.key, cond.comparison, cond.value);

                // 🔧 여기 수정: "entity_id" → "entity_type"
                if ("entity_type".equals(cond.key) && "eq".equals(cond.comparison)) {
                    //AnsanPack.LOGGER.info("[미션 디버그] 비교 대상: killedEntityId='{}', condition.value='{}'", killedEntityId, cond.value);

                    if (!killedEntityId.equals(cond.value)) {
                        match = false;
                        break;
                    } else {
                        match = true;
                    }
                }
            }

            //logProgress(player, mission, def, "kill_entity", match + " - entityId=" + killedEntityId);

            if (match) {
                mission.progress++;
                if (mission.progress >= def.goalValue) {
                    mission.completed = true;
                }

                try (Connection conn = MissionDB.getConnection()) {
                    PlayerMissionDAO.saveOrUpdatePlayerMission(conn, mission);
                } catch (Exception e) {
                    AnsanPack.LOGGER.error("킬 미션 저장 실패", e);
                }
            }
        }
    }

    public static void onItemCrafted(ServerPlayer player, ItemStack craftedItem) {
        String craftedItemId = ForgeRegistries.ITEMS.getKey(craftedItem.getItem()).toString();

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            if (mission.completed) continue; // 여기를 수정했음

            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"craft_item".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty();

            for (MissionCondition cond : conditions) {
                if ("item_id".equals(cond.key) && "eq".equals(cond.comparison)) {
                    if (!craftedItemId.equals(cond.value)) {
                        match = false;
                        break;
                    } else {
                        match = true;
                    }
                }
            }

            //logProgress(player, mission, def, "craft_item", match + " - itemId=" + craftedItemId);

            if (match) {
                mission.progress++;
                if (mission.progress >= def.goalValue) {
                    mission.completed = true;
                }

                try (Connection conn = MissionDB.getConnection()) {
                    PlayerMissionDAO.saveOrUpdatePlayerMission(conn, mission);
                } catch (Exception e) {
                    AnsanPack.LOGGER.error("제작 미션 저장 실패", e);
                }
            }
        }
    }

    public static void onItemCooked(ServerPlayer player, ItemStack result) {
        String resultItemId = ForgeRegistries.ITEMS.getKey(result.getItem()).toString();

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            if (mission.completed) continue; // 여기를 수정했음

            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"cook".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty();

            for (MissionCondition cond : conditions) {
                if ("item_id".equals(cond.key) && "eq".equals(cond.comparison)) {
                    if (!resultItemId.equals(cond.value)) {
                        match = false;
                        break;
                    } else {
                        match = true;
                    }
                }
            }

            //logProgress(player, mission, def, "cook", match + " - resultItemId=" + resultItemId);

            if (match) {
                mission.progress++;
                if (mission.progress >= def.goalValue) {
                    mission.completed = true;
                }

                try (Connection conn = MissionDB.getConnection()) {
                    PlayerMissionDAO.saveOrUpdatePlayerMission(conn, mission);
                } catch (Exception e) {
                    AnsanPack.LOGGER.error("요리 미션 저장 실패", e);
                }
            }
        }
    }

    public static void onBlockMined(ServerPlayer player, String blockId) {
        // 🎯 먼저 블록 정보 가져오기
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        if (block == null) return;

        BlockState state = block.defaultBlockState(); // 블록 상태

        // 🎯 들고 있는 아이템이 곡괭이인지 체크
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof PickaxeItem)) {
            // 곡괭이가 아닌 경우 무시
            return;
        }

        // 🎯 실제 채굴 가능 여부 확인
        if (!heldItem.isCorrectToolForDrops(state)) {
            // 해당 도구로 채굴 불가능한 블록이면 무시
            return;
        }

        // 🔁 기존 처리 계속
        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());
        for (PlayerMissionData mission : missions) {
            if (mission.completed) continue;

            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"mine_block".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty();

            for (MissionCondition cond : conditions) {
                if ("block_id".equals(cond.key) && "eq".equals(cond.comparison)) {
                    if (!blockId.equals(cond.value)) {
                        match = false;
                        break;
                    } else {
                        match = true;
                    }
                }
            }

           // logProgress(player, mission, def, "mine_block", match + " - blockId=" + blockId);

            if (match) {
                mission.progress++;
                if (mission.progress >= def.goalValue) {
                    mission.completed = true;
                }

                try (Connection conn = MissionDB.getConnection()) {
                    PlayerMissionDAO.saveOrUpdatePlayerMission(conn, mission);
                } catch (Exception e) {
                    AnsanPack.LOGGER.error("채광 미션 저장 실패", e);
                }
            }
        }
    }


//모든 광물을 아무거나 들고 캐도 적용돼서 곡괭이로 캐는것만으로 바꿈
//    public static void onBlockMined(ServerPlayer player, String blockId) {
//        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());
//
//        for (PlayerMissionData mission : missions) {
//            if (mission.completed) continue; // 여기를 수정했음
//
//            MissionData def = MissionManager.getMission(mission.missionId);
//            if (def == null || !"mine_block".equals(def.goalType)) continue;
//
//            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
//            boolean match = conditions.isEmpty();
//
//            for (MissionCondition cond : conditions) {
//                if ("block_id".equals(cond.key) && "eq".equals(cond.comparison)) {
//                    if (!blockId.equals(cond.value)) {
//                        match = false;
//                        break;
//                    } else {
//                        match = true;
//                    }
//                }
//            }
//
//            logProgress(player, mission, def, "mine_block", match + " - blockId=" + blockId);
//
//            if (match) {
//                mission.progress++;
//                if (mission.progress >= def.goalValue) {
//                    mission.completed = true;
//                }
//
//                try (Connection conn = MissionDB.getConnection()) {
//                    PlayerMissionDAO.saveOrUpdatePlayerMission(conn, mission);
//                } catch (Exception e) {
//                    AnsanPack.LOGGER.error("채광 미션 저장 실패", e);
//                }
//            }
//        }
//    }

    public static void onPlayerMoved(ServerPlayer player, double distance) {
        if (distance <= 0) return;

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            if (mission.completed) continue; // 여기를 수정했음

            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"move".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty();

            logProgress(player, mission, def, "move", match + " - distance=" + (int)distance);

            if (match) {
                mission.progress += (int) distance;
                if (mission.progress >= def.goalValue) {
                    mission.completed = true;
                }

                try (Connection conn = MissionDB.getConnection()) {
                    PlayerMissionDAO.saveOrUpdatePlayerMission(conn, mission);
                } catch (Exception e) {
                    AnsanPack.LOGGER.error("이동 미션 저장 실패", e);
                }
            }
        }
    }

    public static void onPlayerSlept(ServerPlayer player) {
        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            if (mission.completed) continue; // 여기를 수정했음

            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"sleep".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty();

          //  logProgress(player, mission, def, "sleep", String.valueOf(match));

            if (match) {
                mission.progress++;
                if (mission.progress >= def.goalValue) {
                    mission.completed = true;
                }

                try (Connection conn = MissionDB.getConnection()) {
                    PlayerMissionDAO.saveOrUpdatePlayerMission(conn, mission);
                } catch (Exception e) {
                    AnsanPack.LOGGER.error("수면 미션 저장 실패", e);
                }
            }
        }
    }
    private static void logProgress(ServerPlayer player, PlayerMissionData mission, MissionData def, String type, String detail) {
        AnsanPack.LOGGER.info("[미션] UUID={}, 미션ID={}, 유형={}, 조건={}, 진행도={}/{}{}",
                player.getStringUUID(), mission.missionId, type, detail,
                mission.progress, def.goalValue, mission.completed ? " (완료)" : "");
    }
}
