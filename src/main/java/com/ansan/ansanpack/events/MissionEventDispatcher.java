package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.MissionManager;
import com.ansan.ansanpack.mission.*;

import java.sql.Connection;
import java.util.List;

import net.minecraft.server.level.ServerPlayer;

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
}
