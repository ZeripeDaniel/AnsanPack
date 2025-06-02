package com.ansan.ansanpack.config;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class UpgradeConfigManager {
    private static final Map<ResourceLocation, UpgradeConfig> ITEM_CONFIGS = new HashMap<>();

    public static class UpgradeConfig {
        public final ResourceLocation item;
        public final int maxLevel;
        public final Map<String, Double> effects;

        public UpgradeConfig(ResourceLocation item, int maxLevel, Map<String, Double> effects) {
            this.item = item;
            this.maxLevel = maxLevel;
            this.effects = effects;
        }

        public Item getItem() {
            return ForgeRegistries.ITEMS.getValue(item);
        }
    }

    public static void loadConfigFromMySQL() {
        ITEM_CONFIGS.clear();

        Properties props = loadDbProps();
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" + props.getProperty("db.port") + "/" +
                props.getProperty("db.database") + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"))) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM upgrades");

            while (rs.next()) {
                ResourceLocation itemId = new ResourceLocation(rs.getString("item_id"));
                int maxLevel = rs.getInt("max_level");

                Map<String, Double> effects = new HashMap<>();
                try (PreparedStatement effectStmt = conn.prepareStatement("SELECT effect_key, effect_value FROM upgrade_effects WHERE item_id = ?")) {
                    effectStmt.setString(1, itemId.toString());
                    ResultSet ers = effectStmt.executeQuery();
                    while (ers.next()) {
                        effects.put(ers.getString("effect_key"), ers.getDouble("effect_value"));
                    }
                }

                UpgradeConfig config = new UpgradeConfig(itemId, maxLevel, effects);
                ITEM_CONFIGS.put(itemId, config);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("[AnsanPack] MySQL 접속 실패: " + url + "\n원인: " + e.getMessage(), e);
        }
    }

    public static Properties loadDbProps() {
        Path configPath = Path.of("config/ansanpack_db.properties");
        Properties props = new Properties();

        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath.getParent());
                try (var writer = Files.newBufferedWriter(configPath)) {
                    writer.write("db.host=127.0.0.1\n");
                    writer.write("db.port=3306\n");
                    writer.write("db.database=minecraft\n");
                    writer.write("db.user=root\n");
                    writer.write("db.password=your_password\n");
                }
                System.out.println("[AnsanPack] MySQL 설정파일 생성됨: " + configPath);
            } catch (IOException e) {
                throw new RuntimeException("MySQL 설정파일 생성 실패: " + configPath, e);
            }
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            props.load(reader);
        } catch (IOException e) {
            throw new RuntimeException("MySQL 설정파일 읽기 실패: " + configPath, e);
        }

        return props;
    }

    public static double getEffectValue(ResourceLocation itemId, String effectKey) {
        UpgradeConfig config = ITEM_CONFIGS.get(itemId);
        if (config != null && config.effects.containsKey(effectKey)) {
            return config.effects.get(effectKey);
        }
        return 0.0;
    }

    public static Optional<UpgradeConfig> getConfig(Item item) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return Optional.ofNullable(ITEM_CONFIGS.get(itemId));
    }

    public static Optional<UpgradeConfig> getItemConfig(ResourceLocation itemId) {
        return Optional.ofNullable(ITEM_CONFIGS.get(itemId));
    }

    public static double getAnyEffectValue(ResourceLocation itemId, String... effectKeys) {
        for (String key : effectKeys) {
            double value = getEffectValue(itemId, key);
            if (value != 0.0) return value;
        }
        return 0.0;
    }
}
