// ✅ AnsanPack: 하루 1회 일일 미션 다시받기 기능 구현

package com.ansan.ansanpack.mission;

import com.ansan.ansanpack.config.MissionManager;
import com.ansan.ansanpack.mission.MissionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MissionResetService {

    public static boolean canResetDailyMissions(String playerUUID) {
        try (Connection conn = MissionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT last_reset FROM player_mission_reset WHERE player_uuid = ?")) {

            stmt.setString(1, playerUUID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date lastReset = rs.getDate("last_reset");
                    return lastReset == null || !lastReset.toLocalDate().equals(LocalDate.now());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true; // 예외 발생 시 일단 허용
    }

    public static boolean resetDailyMissions(String playerUUID) {
        try (Connection conn = MissionDB.getConnection()) {
            conn.setAutoCommit(false);

            // 기존 일일 미션 삭제
            try (PreparedStatement deleteStmt = conn.prepareStatement(
                    "DELETE FROM player_mission WHERE player_uuid = ? AND type = 'daily'")) {
                deleteStmt.setString(1, playerUUID);
                deleteStmt.executeUpdate();
            }

            // ✅ 현재 시간 기준 Timestamp 준비
            Timestamp now = getCurrentTimestamp(conn);

            // 새로운 미션 3개 선택
            List<MissionData> newMissions = MissionManager.getRandomMissions("daily", 3);
            for (MissionData data : newMissions) {
                PlayerMissionData m = new PlayerMissionData(playerUUID, data.id, 0, false, false, now);
                m.type = data.type;
                m.description = data.description;
                m.goalValue = data.goalValue; // 있으면 복사
                PlayerMissionDAO.saveOrUpdatePlayerMission(conn, m);
            }

            // reset 기록 업데이트
            try (PreparedStatement upsertStmt = conn.prepareStatement(
                    "INSERT INTO player_mission_reset (player_uuid, last_reset) VALUES (?, ?) " +
                            "ON DUPLICATE KEY UPDATE last_reset = ?")) {
                Date today = Date.valueOf(LocalDate.now());
                upsertStmt.setString(1, playerUUID);
                upsertStmt.setDate(2, today);
                upsertStmt.setDate(3, today);
                upsertStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private static Timestamp getCurrentTimestamp(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT NOW()");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getTimestamp(1);
            } else {
                throw new SQLException("NOW() 조회 실패");
            }
        }
    }




}
