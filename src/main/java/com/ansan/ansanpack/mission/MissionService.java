package com.ansan.ansanpack.mission;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.MissionManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;

public class MissionService {
    private static final Map<String, List<PlayerMissionData>> playerMissionCache = new HashMap<>();

    public static List<PlayerMissionData> getOrAssignMissions(String uuid) {
        if (playerMissionCache.containsKey(uuid)) {
            return playerMissionCache.get(uuid);
        }

        try (Connection conn = MissionDB.getConnection()) {
            List<PlayerMissionData> existing = PlayerMissionDAO.loadMissionsForPlayer(conn, uuid);

            boolean needsReassign = false;
            LocalDate today = LocalDate.now();
            int currentWeek = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int currentYear = today.get(IsoFields.WEEK_BASED_YEAR);

            for (PlayerMissionData data : existing) {
                var def = MissionManager.getMission(data.missionId);
                if (def != null) {
                    data.type = def.type;
                    data.description = def.description;

                    // 여기를 수정했음: Timestamp → LocalDate 변환
                    LocalDate assignedDate = data.assignedAt.toLocalDateTime().toLocalDate();

                    if ("daily".equals(def.type) && assignedDate.isBefore(today)) {
                        needsReassign = true;
                        break;
                    } else if ("weekly".equals(def.type)) {
                        int assignedWeek = assignedDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                        int assignedYear = assignedDate.get(IsoFields.WEEK_BASED_YEAR);
                        if (assignedWeek != currentWeek || assignedYear != currentYear) {
                            needsReassign = true;
                            break;
                        }
                    }
                }
            }

            if (!existing.isEmpty() && !needsReassign) {
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
                data.description = mission.description;
                PlayerMissionDAO.saveOrUpdatePlayerMission(conn, data);
                assigned.add(data);
                AnsanPack.LOGGER.info("[미션할당] UUID={}, 신규 {} 미션 할당됨: ID={}, 설명={}", uuid, mission.type, mission.id, mission.description);
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

    public static void clearAllCache() {
        playerMissionCache.clear();
    }
}
