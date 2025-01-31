package com.ansan.ansanpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RandomBoxConfigManager {
    private static final String CONFIG_FILE = "ansanpack_random_box_items.json";
    private static List<RandomBoxItem> items = new ArrayList<>();

    public static void loadConfig() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<RandomBoxItem>>(){}.getType();
            items = gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createDefaultConfig(Path configPath) {
        List<RandomBoxItem> defaultItems = new ArrayList<>();
        defaultItems.add(new RandomBoxItem("minecraft:diamond", 1, 0.1, "다이아몬드"));
        defaultItems.add(new RandomBoxItem("minecraft:iron_ingot", 1, 0.3, "철 주괴"));
        defaultItems.add(new RandomBoxItem("minecraft:gold_ingot", 1, 0.2, "금 주괴"));
        defaultItems.add(new RandomBoxItem("minecraft:emerald", 1, 0.15, "에메랄드"));

        try (Writer writer = Files.newBufferedWriter(configPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(defaultItems, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<RandomBoxItem> getItems() {
        return items;
    }

    public static class RandomBoxItem {
        public String itemName;
        public int count;
        public double probability;
        public String giftname;  // 새로 추가된 필드

        public RandomBoxItem(String itemName, int count, double probability, String giftname) {
            this.itemName = itemName;
            this.count = count;
            this.probability = probability;
            this.giftname = giftname;
        }

        public Item getItem() {
            ResourceLocation resourceLocation = new ResourceLocation(itemName);
            Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
            return item != null ? item : Items.AIR;
        }
    }

}
