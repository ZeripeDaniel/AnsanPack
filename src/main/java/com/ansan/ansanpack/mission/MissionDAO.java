package com.ansan.ansanpack.mission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MissionDAO {


//    public static List<MissionData> loadAllMissions() throws SQLException {
//        List<MissionData> result = new ArrayList<>();
//        try (Connection conn = MissionDB.getConnection();
//             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM missions");
//             ResultSet rs = stmt.executeQuery()) {
//            while (rs.next()) {
//                Integer rewardId = rs.getObject("reward_id") != null ? rs.getInt("reward_id") : null;
//
//                result.add(new MissionData(
//                        rs.getString("id"),
//                        rs.getString("type"),
//                        rs.getString("description"),
//                        rs.getString("goal_type"),
//                        rs.getInt("goal_value"),
//                        rewardId,
//                        rs.getInt("priority"),
//                        rs.getString("requires")
//                ));
//            }
//        } catch (Exception e) {
//            throw new SQLException("미션 목록 로딩 실패", e);
//        }
//        return result;
//    }
public static List<MissionData> loadAllMissions() throws SQLException {
    List<MissionData> result = new ArrayList<>();
    try (Connection conn = MissionDB.getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT * FROM missions");
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Object rewardObj = rs.getObject("reward_id");
            Integer rewardId = (rewardObj != null) ? ((Number) rewardObj).intValue() : null;

            String id = rs.getString("id");
            if (id == null || id.isBlank()) {
                System.err.println("[AnsanPack] id가 비어있는 미션 건너뜀");
                continue;
            }

            result.add(new MissionData(
                    id,
                    rs.getString("type"),
                    rs.getString("description"),
                    rs.getString("goal_type"),
                    rs.getInt("goal_value"),
                    rewardId,
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
                int id = rs.getInt("id");
                String itemId = rs.getString("item_id");
                int rewardValue = rs.getInt("value");
                String rewardType = rs.getString("reward_type");
                String type = rs.getString("type");

                if (rewardType == null || type == null) {
                    System.err.println("[AnsanPack] 잘못된 보상 항목 무시됨 (id: " + id + ", reward_type 또는 type이 null)");
                    continue;
                }

                result.add(new MissionReward(id, itemId, rewardValue, rewardType, type));
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
