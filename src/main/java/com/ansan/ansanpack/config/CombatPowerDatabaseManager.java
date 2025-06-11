package com.ansan.ansanpack.config;

import com.ansan.ansanpack.AnsanPack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class CombatPowerDatabaseManager {

    public static void saveCombatPower(UUID uuid, String playerName, double power) {
        var props = UpgradeConfigManager.loadDbProps();  // 👉 재사용용 함수로 따로 추출할 예정
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" +
                props.getProperty("db.port") + "/" +
                props.getProperty("db.database") +
                "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"))) {
            String sql = """
                INSERT INTO player_combat_power (uuid, player_name, power)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    player_name = VALUES(player_name),
                    power = VALUES(power),
                    updated_at = CURRENT_TIMESTAMP
                """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, playerName);
                ps.setDouble(3, power);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            AnsanPack.LOGGER.error("[CombatPower] 저장 실패 (uuid={}, name={}, power={}) → {}", uuid, playerName, power, e.getMessage());
        }
    }
}
