package com.ansan.ansanpack.config;

import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import com.ansan.ansanpack.server.stat.PlayerStat;

import java.sql.*;
import java.util.Properties;
import java.util.UUID;

public class StatDatabaseManager {

    private static Connection getConnection() throws SQLException {
        Properties props = UpgradeConfigManager.loadDbProps(); // ✅ 재사용
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" +
                props.getProperty("db.port") + "/" +
                props.getProperty("db.database") +
                "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        return DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"));
    }

    // ✅ 스탯 저장
    // 신규 메서드: 저장용
    public static void saveStats(UUID uuid, String playerName, PlayerStat data) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO player_stats (uuid, player_name, str, agi, intel, luck, available_ap) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE player_name=?, str=?, agi=?, intel=?, luck=?, available_ap=?"
            );
            stmt.setString(1, uuid.toString());
            stmt.setString(2, playerName);
            stmt.setInt(3, data.getStrength());
            stmt.setInt(4, data.getAgility());
            stmt.setInt(5, data.getIntelligence());
            stmt.setInt(6, data.getLuck());
            stmt.setInt(7, data.getAvailableAP());

            stmt.setString(8, playerName);
            stmt.setInt(9, data.getStrength());
            stmt.setInt(10, data.getAgility());
            stmt.setInt(11, data.getIntelligence());
            stmt.setInt(12, data.getLuck());
            stmt.setInt(13, data.getAvailableAP());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[AnsanPack] 스탯 저장 실패: " + e.getMessage(), e);
        }
    }

    // 신규 메서드: 로드용
    public static PlayerStat loadStats(UUID uuid) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM player_stats WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new PlayerStat(
                        rs.getInt("str"),
                        rs.getInt("agi"),
                        rs.getInt("intel"),
                        rs.getInt("luck"),
                        rs.getInt("available_ap")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("[AnsanPack] 스탯 불러오기 실패: " + e.getMessage(), e);
        }

        return new PlayerStat(0, 0, 0, 0, 0); // 기본값
    }

}
