package com.ansan.ansanpack.mission;

import com.ansan.ansanpack.AnsanPack;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MissionConditionDAO {

    public static List<MissionCondition> getConditions(String missionId) {
        List<MissionCondition> list = new ArrayList<>();
        String query = "SELECT * FROM mission_conditions WHERE mission_id = ?";
        try (Connection conn = MissionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, missionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new MissionCondition(
                        rs.getInt("id"),
                        rs.getString("mission_id"),
                        rs.getString("key"),
                        rs.getString("value"),
                        rs.getString("comparison")
                ));
            }
        } catch (Exception e) {
            AnsanPack.LOGGER.error("미션 조건 로딩 실패", e);
        }
        return list;
    }
}
