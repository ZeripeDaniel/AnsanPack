package com.ansan.ansanpack.config;

import com.ansan.ansanpack.AnsanPack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnvilRecipeManager {

    public record AnvilRecipe(Item insertItem, Item resourceItem, int stack, Item resultItem, int costLevel, boolean isTierUpgrade) {}

    private static final List<AnvilRecipe> RECIPES = new ArrayList<>();

    public static List<AnvilRecipe> getRecipes() {
        return RECIPES;
    }

    public static void loadFromDatabase() {
        RECIPES.clear();

        var props = UpgradeConfigManager.loadDbProps();
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" + props.getProperty("db.port") + "/" +
                props.getProperty("db.database") + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"));
             PreparedStatement stmt = conn.prepareStatement("SELECT insert_item, resource_item, stack, result_item, cost_level, tier_upgrade FROM anvil_recipes");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int stack = rs.getInt("stack");
                int cost = rs.getInt("cost_level");
                boolean tierUpgrade = rs.getBoolean("tier_upgrade");

                ResourceLocation insertId = new ResourceLocation(rs.getString("insert_item"));
                ResourceLocation resourceId = new ResourceLocation(rs.getString("resource_item"));
                ResourceLocation resultId = new ResourceLocation(rs.getString("result_item"));

                Item insertItem = ForgeRegistries.ITEMS.getValue(insertId);
                Item resourceItem = ForgeRegistries.ITEMS.getValue(resourceId);
                Item resultItem = ForgeRegistries.ITEMS.getValue(resultId);

                if (insertItem != null && resourceItem != null && resultItem != null) {
                    RECIPES.add(new AnvilRecipe(insertItem, resourceItem, stack, resultItem, cost, tierUpgrade));
                } else {
                    AnsanPack.LOGGER.warn("[AnsanPack] 잘못된 Anvil 레시피 - insertItem: {}, resourceItem: {}, resultItem: {}",
                            insertId, resourceId, resultId);
                }
            }

        } catch (SQLException e) {
            AnsanPack.LOGGER.error("MySQL anvil recipe load failed", e);
        }
    }

}
