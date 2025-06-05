package com.ansan.ansanpack.mission;

import com.ansan.ansanpack.AnsanPack;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerMissionDAO {

    public static List<PlayerMissionData> loadMissionsForPlayer(Connection conn, String uuid) throws SQLException {
        List<PlayerMissionData> result = new ArrayList<>();
        String query = "SELECT * FROM player_missions WHERE uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new PlayerMissionData(
                            rs.getString("uuid"),
                            rs.getString("mission_id"),
                            rs.getInt("progress"),
                            rs.getBoolean("completed"),
                            rs.getBoolean("rewarded"), // ✅ 통일
                            rs.getTimestamp("assigned_at") // ✅ assignedAt으로 맵핑
                    ));
                }
            }
        }
        return result;
    }

    public static void saveOrUpdatePlayerMission(Connection conn, PlayerMissionData data) throws SQLException {
        String sql = """
            INSERT INTO player_missions (uuid, mission_id, progress, completed, rewarded, assigned_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                progress = VALUES(progress),
                completed = VALUES(completed),
                rewarded = VALUES(rewarded),
                assigned_at = VALUES(assigned_at)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, data.uuid);
            stmt.setString(2, data.missionId);
            stmt.setInt(3, data.progress);
            stmt.setBoolean(4, data.completed);
            stmt.setBoolean(5, data.rewarded);
            stmt.setTimestamp(6, data.assignedAt);
            stmt.executeUpdate();
        }
        AnsanPack.LOGGER.debug("[DB] 미션 저장: UUID={}, 미션ID={}, 진행도={}, 완료여부={}", data.uuid, data.missionId, data.progress, data.completed);

    }
}
