package com.ansan.ansanpack.mission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MissionDAO {

    public static List<MissionData> loadAllMissions() throws SQLException {
        List<MissionData> result = new ArrayList<>();
        try (Connection conn = MissionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM missions");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new MissionData(
                        rs.getString("id"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getString("goal_type"),
                        rs.getInt("goal_value"),
                        (Integer) rs.getObject("reward_id"), // nullable
                        rs.getInt("priority"),
                        rs.getString("requires")
                ));
            }
        } catch (Exception e) {
            throw new SQLException("미션 목록 로딩 실패", e);
        }
        return result;
    }

    public static List<MissionReward> loadAllRewards() throws SQLException {
        List<MissionReward> result = new ArrayList<>();
        try (Connection conn = MissionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM mission_rewards");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new MissionReward(
                        rs.getInt("id"),
                        rs.getString("item_id"),
                        rs.getInt("reward_value"),
                        rs.getString("reward_type"),
                        rs.getString("type")
                ));
            }
        } catch (Exception e) {
            throw new SQLException("보상 목록 로딩 실패", e);
        }
        return result;
    }

    // ✅ 보상 수령 여부 갱신
    public static void markMissionRewarded(String uuid, String missionId) {
        String sql = "UPDATE player_missions SET rewarded = TRUE WHERE uuid = ? AND mission_id = ?";
        try (Connection conn = MissionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, missionId);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("보상 수령 상태 갱신 실패", e);
        }
    }
}
