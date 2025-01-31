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

public class ConfigManager {
    private static final String CONFIG_FILE = "ansanpack_anvil_recipes.json";
    private static List<AnvilRecipe> recipes = new ArrayList<>();

    public static void loadConfig() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<AnvilRecipe>>(){}.getType();
            recipes = gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createDefaultConfig(Path configPath) {
        List<AnvilRecipe> defaultRecipes = new ArrayList<>();
        defaultRecipes.add(new AnvilRecipe("minecraft:iron_pickaxe", "minecraft:diamond", 64, "minecraft:diamond_pickaxe", 30));
        defaultRecipes.add(new AnvilRecipe("minecraft:iron_sword", "minecraft:gold_ingot", 32, "minecraft:golden_sword", 30));

        try (Writer writer = Files.newBufferedWriter(configPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(defaultRecipes, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<AnvilRecipe> getRecipes() {
        return recipes;
    }

    public static class AnvilRecipe {
        public String insertItem;
        public String resource;
        public int stack;
        public String result;
        public int costLevel;

        public AnvilRecipe(String insertItem, String resource, int stack, String result,int costLevel) {
            this.insertItem = insertItem;
            this.resource = resource;
            this.stack = stack;
            this.result = result;
            this.costLevel = costLevel;
        }

        public Item getInsertItem() {
          Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(insertItem));
          return item != null ? item : Items.AIR;
        }

        public Item getResourceItem() {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resource));
            return item != null ? item : Items.AIR;
        }

        public Item getResultItem() {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(result));
            return item != null ? item : Items.AIR;
        }
    }
}
