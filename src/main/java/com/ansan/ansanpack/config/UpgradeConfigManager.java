package com.ansan.ansanpack.config;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class UpgradeConfigManager {
    private static final String CONFIG_FILE = "ansanpack_upgrades.json";
    private static final Map<ResourceLocation, UpgradeConfig> ITEM_CONFIGS = new HashMap<>();

    public static class UpgradeConfig {
        public final ResourceLocation item;
        public final int maxLevel;
        public final double baseChance;
        public final double chanceDecrease;
        public final Map<String, Double> effects;

        public UpgradeConfig(JsonObject obj) {
            this.item = new ResourceLocation(obj.get("item").getAsString());
            this.maxLevel = obj.get("max_level").getAsInt();
            this.baseChance = obj.get("base_chance").getAsDouble();
            this.chanceDecrease = obj.get("chance_decrease").getAsDouble();

            this.effects = new HashMap<>();
            JsonObject effectsObj = obj.getAsJsonObject("effects");
            for (Map.Entry<String, JsonElement> entry : effectsObj.entrySet()) {
                effects.put(entry.getKey(), entry.getValue().getAsDouble());
            }
        }

        public Item getItem() {
            return ForgeRegistries.ITEMS.getValue(item);
        }
    }

    public static void loadConfig() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);

        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(reader).getAsJsonObject();
            JsonArray upgrades = root.getAsJsonArray("upgrades");

            ITEM_CONFIGS.clear();
            for (JsonElement elem : upgrades) {
                UpgradeConfig config = new UpgradeConfig(elem.getAsJsonObject());
                ITEM_CONFIGS.put(config.item, config);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createDefaultConfig(Path path) {
        List<UpgradeConfig> defaults = new ArrayList<>();

        JsonObject sample1 = new JsonObject();
        sample1.addProperty("item", "minecraft:diamond_sword");
        sample1.addProperty("max_level", 10);
        sample1.addProperty("base_chance", 0.6);
        sample1.addProperty("chance_decrease", 0.05);
        JsonObject effects1 = new JsonObject();
        effects1.addProperty("damage_per_level", 0.1);
        sample1.add("effects", effects1);
        defaults.add(new UpgradeConfig(sample1));

        try (Writer writer = Files.newBufferedWriter(path)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject root = new JsonObject();
            JsonArray array = new JsonArray();
            defaults.forEach(c -> array.add(convertToJson(c)));
            root.add("upgrades", array);
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonObject convertToJson(UpgradeConfig config) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", config.item.toString());
        obj.addProperty("max_level", config.maxLevel);
        obj.addProperty("base_chance", config.baseChance);
        obj.addProperty("chance_decrease", config.chanceDecrease);

        JsonObject effects = new JsonObject();
        config.effects.forEach(effects::addProperty);
        obj.add("effects", effects);

        return obj;
    }
    public static double getEffectValue(ResourceLocation itemId, String effectKey) {
        UpgradeConfig config = ITEM_CONFIGS.get(itemId);
        if (config != null && config.effects.containsKey(effectKey)) {
            return config.effects.get(effectKey);
        }
        return 0.0; // 기본값 반환
    }

    public static Optional<UpgradeConfig> getConfig(Item item) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return Optional.ofNullable(ITEM_CONFIGS.get(itemId));
    }

    public static Optional<UpgradeConfig> getItemConfig(ResourceLocation itemId) {
        return Optional.ofNullable(ITEM_CONFIGS.get(itemId));
    }
}
