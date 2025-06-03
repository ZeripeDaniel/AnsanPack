package com.ansan.ansanpack.config;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.sql.*;
import java.util.*;

public class MobDropManager {
    public static record DropEntry(ResourceLocation itemId, double chance, int count) {}

    private static final List<DropEntry> drops = new ArrayList<>();

    public static void loadFromMySQL() {
        drops.clear();
        try {
            Properties props = UpgradeConfigManager.loadDbProps(); // 공용 DB 설정 재사용
            String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" +
                    props.getProperty("db.port") + "/" +
                    props.getProperty("db.database") +
                    "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

            try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"))) {
                PreparedStatement stmt = conn.prepareStatement("SELECT item_id, chance, count FROM mob_drops WHERE enabled = TRUE");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String itemIdStr = rs.getString("item_id");
                    double chance = rs.getDouble("chance");
                    int count = rs.getInt("count");

                    ResourceLocation itemId = new ResourceLocation(itemIdStr);
                    drops.add(new DropEntry(itemId, chance, count));
                }

                AnsanPack.LOGGER.info("[AnsanPack] 몹 드랍 아이템 {}개 로딩됨", drops.size());
            }
        } catch (Exception e) {
            AnsanPack.LOGGER.error("[AnsanPack] 몹 드랍 로딩 실패", e);
        }
    }

    public static List<DropEntry> getDrops() {
        return drops;
    }
}
