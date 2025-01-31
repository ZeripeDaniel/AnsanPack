package com.ansan.ansanpack.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public class EntityConfigManager {
    private static final String CONFIG_FILE = "ansanpack_entity_attributes.json";
    private static Map<String, EntityAttributes> entityAttributesMap = new HashMap<>();

    public static class EntityAttributes {
        public double maxHealth;
        public double movementSpeed;
        public double armor;
        public double attackDamage;

        public EntityAttributes(double maxHealth, double movementSpeed, double armor, double attackDamage) {
            this.maxHealth = maxHealth;
            this.movementSpeed = movementSpeed;
            this.armor = armor;
            this.attackDamage = attackDamage;
        }
    }

    public static void loadConfig() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, EntityAttributes>>(){}.getType();
            entityAttributesMap = gson.fromJson(reader, mapType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createDefaultConfig(Path configPath) {
        Map<String, EntityAttributes> defaultConfig = new HashMap<>();
        defaultConfig.put("animalistic_a:ninfa", new EntityAttributes(30.0, 0.3, 5.0, 3.0));
        defaultConfig.put("animalistic_a:another_entity", new EntityAttributes(20.0, 0.25, 2.0, 2.0));

        try (Writer writer = Files.newBufferedWriter(configPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(defaultConfig, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static EntityAttributes getEntityAttributes(String entityId) {
        return entityAttributesMap.get(entityId);
    }

    public static Map<String, EntityAttributes> getAllEntityAttributes() {
        return entityAttributesMap;
    }
}

