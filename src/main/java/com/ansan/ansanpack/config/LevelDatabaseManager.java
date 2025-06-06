package com.ansan.ansanpack.config;

import com.ansan.ansanpack.AnsanPack;

import java.sql.*;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiConsumer;

public class LevelDatabaseManager {

    public static void saveOrUpdate(UUID uuid, String playerName, int level, int exp) {
        Properties props = UpgradeConfigManager.loadDbProps();

        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" + props.getProperty("db.port") + "/" +
                props.getProperty("db.database") + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"))) {
            // 테이블 없으면 생성
            try (Statement tableCheck = conn.createStatement()) {
                tableCheck.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_levels (
                        uuid VARCHAR(36) PRIMARY KEY,
                        player_name VARCHAR(50),
                        level INT NOT NULL,
                        exp INT NOT NULL,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                """);
            }

            // UPSERT 처리
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO player_levels (uuid, player_name, level, exp)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    player_name = VALUES(player_name),
                    level = VALUES(level),
                    exp = VALUES(exp)
            """)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, playerName);
                stmt.setInt(3, level);
                stmt.setInt(4, exp);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            AnsanPack.LOGGER.error("[LevelDB] 레벨 저장 실패: {}", e.getMessage());
        }
    }
    public static void load(UUID uuid, BiConsumer<Integer, Integer> callback) {
        Properties props = UpgradeConfigManager.loadDbProps();

        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" + props.getProperty("db.port") + "/" +
                props.getProperty("db.database") + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"))) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT level, exp FROM player_levels WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int level = rs.getInt("level");
                    int exp = rs.getInt("exp");
                    callback.accept(level, exp);
                } else {
                    callback.accept(1, 0); // 기본값: 1레벨, 0 경험치
                }
            }
        } catch (SQLException e) {
            AnsanPack.LOGGER.error("[LevelDB] 레벨 불러오기 실패: {}", e.getMessage());
            callback.accept(1, 0); // 실패 시 기본값
        }
    }
}
