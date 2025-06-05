package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.MissionManager;
import com.ansan.ansanpack.mission.*;

import java.sql.Connection;
import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class MissionEventDispatcher {

    public static void onUpgradeAttempt(ServerPlayer player, boolean success) {
        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"upgrade".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty(); // 조건 없으면 기본 허용

            for (MissionCondition cond : conditions) {
                if ("result".equals(cond.key)) {
                    if ("success".equals(cond.value) && "eq".equals(cond.comparison) && !success) match = false;
                    if ("fail".equals(cond.value) && "eq".equals(cond.comparison) && success) match = false;
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
                    AnsanPack.LOGGER.error("강화 미션 저장 실패", e);
                }
            }
        }
    }

    // ✅ kill_entity 미션 처리
    public static void onKillEntity(ServerPlayer player, EntityType<?> killedEntityType) {
        String killedEntityId = ForgeRegistries.ENTITY_TYPES.getKey(killedEntityType).toString();

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"kill_entity".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty(); // 조건 없으면 기본 허용

            for (MissionCondition cond : conditions) {
                if ("entity_id".equals(cond.key) && "eq".equals(cond.comparison)) {
                    if (!killedEntityId.equals(cond.value)) {
                        match = false;
                        break;
                    } else {
                        match = true;
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
                    AnsanPack.LOGGER.error("킬 미션 저장 실패", e);
                }
            }
        }
    }

    // ✅ craft_item 미션 처리
    public static void onItemCrafted(ServerPlayer player, ItemStack craftedItem) {
        String craftedItemId = ForgeRegistries.ITEMS.getKey(craftedItem.getItem()).toString();

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"craft_item".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty(); // 조건 없으면 기본 허용

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

    // ✅ cook 미션 처리
    public static void onItemCooked(ServerPlayer player, ItemStack result) {
        String resultItemId = ForgeRegistries.ITEMS.getKey(result.getItem()).toString();

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"cook".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty(); // 조건 없으면 통과

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

    // ✅ mine_block 미션 처리
    public static void onBlockMined(ServerPlayer player, String blockId) {
        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"mine_block".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty(); // 조건 없으면 허용

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

    // ✅ move 미션 처리
    public static void onPlayerMoved(ServerPlayer player, double distance) {
        if (distance <= 0) return;

        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"move".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty(); // 조건이 없으면 통과

            // 이동 조건은 현재 특별히 없음, 향후 dim_id 등으로 확장 가능

            if (match) {
                mission.progress += (int) distance; // 누적 거리 (블록 단위)
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

    // ✅ sleep 미션 처리
    public static void onPlayerSlept(ServerPlayer player) {
        List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());

        for (PlayerMissionData mission : missions) {
            MissionData def = MissionManager.getMission(mission.missionId);
            if (def == null || !"sleep".equals(def.goalType)) continue;

            List<MissionCondition> conditions = MissionConditionDAO.getConditions(mission.missionId);
            boolean match = conditions.isEmpty(); // 기본 조건 없음

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





}
