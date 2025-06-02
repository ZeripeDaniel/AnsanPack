package com.ansan.ansanpack.config;

import net.minecraft.resources.ResourceLocation;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UpgradeChanceManager {

    private static final Map<String, Map<Integer, Double>> CHANCES = new HashMap<>();

    public static void loadChancesFromMySQL() {
        CHANCES.clear();

        // 기존 설정 파일에서 MySQL 정보 불러오기
        var props = UpgradeConfigManager.loadDbProps();  // 👉 재사용용 함수로 따로 추출할 예정
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" +
                props.getProperty("db.port") + "/" +
                props.getProperty("db.database") +
                "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"))) {
            PreparedStatement stmt = conn.prepareStatement("SELECT item_id, level, success_chance FROM upgrade_chances");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String itemId = rs.getString("item_id");
                int level = rs.getInt("level");
                double chance = rs.getDouble("success_chance");

                CHANCES.computeIfAbsent(itemId, k -> new HashMap<>()).put(level, chance);
            }

        } catch (SQLException e) {
            throw new RuntimeException("[AnsanPack] 강화 확률 로딩 실패: " + e.getMessage(), e);
        }
    }

    public static double getSuccessChance(ResourceLocation itemId, int level) {
        Map<Integer, Double> levelMap = CHANCES.get(itemId.toString());
        if (levelMap == null) {
            System.out.println("[AnsanPack][확률X] itemId 미존재: " + itemId);
            return 0.0;
        }
        if (!levelMap.containsKey(level)) {
            System.out.println("[AnsanPack][확률X] 해당 레벨 없음: " + level + " for " + itemId);
            return 0.0;
        }
        return levelMap.get(level);
    }

}
