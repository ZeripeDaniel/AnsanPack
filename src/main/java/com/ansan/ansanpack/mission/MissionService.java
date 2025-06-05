package com.ansan.ansanpack.mission;

import com.ansan.ansanpack.config.MissionManager;

import java.sql.*;
import java.util.*;

public class MissionService {
    private static final Map<String, List<PlayerMissionData>> playerMissionCache = new HashMap<>();

    public static List<PlayerMissionData> getOrAssignMissions(String uuid) {
        if (playerMissionCache.containsKey(uuid)) {
            return playerMissionCache.get(uuid);
        }

        try (Connection conn = MissionDB.getConnection()) {
            List<PlayerMissionData> existing = PlayerMissionDAO.loadMissionsForPlayer(conn, uuid);

            for (PlayerMissionData data : existing) {
                var def = MissionManager.getMission(data.missionId);
                if (def != null) {
                    data.type = def.type;
                    data.description = def.description; // ✅ 기존 미션에 설명 세팅
                }
            }

            if (!existing.isEmpty()) {
                playerMissionCache.put(uuid, existing);
                return existing;
            }

            List<MissionData> allMissions = new ArrayList<>(MissionManager.getAllMissions());
            Collections.shuffle(allMissions);

            List<MissionData> selected = new ArrayList<>();
            int dailyCount = 0, weeklyCount = 0;
            for (MissionData m : allMissions) {
                if ("daily".equals(m.type) && dailyCount < 3) {
                    selected.add(m);
                    dailyCount++;
                } else if ("weekly".equals(m.type) && weeklyCount < 2) {
                    selected.add(m);
                    weeklyCount++;
                }
                if (dailyCount == 3 && weeklyCount == 2) break;
            }

            List<PlayerMissionData> assigned = new ArrayList<>();
            Timestamp now = getCurrentTimestamp(conn);
            for (MissionData mission : selected) {
                PlayerMissionData data = new PlayerMissionData(uuid, mission.id, 0, false, false, now);
                data.type = mission.type;
                data.description = mission.description; // ✅ 신규 미션에도 설명 세팅
                PlayerMissionDAO.saveOrUpdatePlayerMission(conn, data);
                assigned.add(data);
            }

            playerMissionCache.put(uuid, assigned);
            return assigned;

        } catch (Exception e) {
            throw new RuntimeException("[AnsanPack] 미션 할당 오류: " + e.getMessage(), e);
        }
    }

    private static Timestamp getCurrentTimestamp(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT NOW()");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getTimestamp(1);
            } else {
                throw new SQLException("NOW() 실패");
            }
        }
    }

    public static void clearCache(String uuid) {
        playerMissionCache.remove(uuid);
    }
}
