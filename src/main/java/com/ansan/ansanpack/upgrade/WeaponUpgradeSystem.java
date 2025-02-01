package com.ansan.ansanpack.upgrade;

import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class WeaponUpgradeSystem {
    private static final String UPGRADE_TAG = "ansan_upgrade";

    public static boolean canUpgrade(ItemStack stack) {
        return UpgradeConfigManager.getConfig(stack.getItem()).isPresent();
    }

    public static double getUpgradeChance(ItemStack stack) {
        return UpgradeConfigManager.getConfig(stack.getItem())
                .map(config -> {
                    int level = getCurrentLevel(stack);
                    return config.baseChance - (level * config.chanceDecrease);
                })
                .orElse(0.0);
    }

    public static int getCurrentLevel(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(UPGRADE_TAG);
    }

    public static void applyUpgrade(ItemStack stack, boolean success) {
        UpgradeConfigManager.getConfig(stack.getItem()).ifPresent(config -> {
            int currentLevel = getCurrentLevel(stack);
            int newLevel = success ?
                    Math.min(currentLevel + 1, config.maxLevel) :
                    Math.max(currentLevel - 1, 0);

            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt(UPGRADE_TAG, newLevel);
            applyEffects(tag, config, newLevel);
        });
    }

    private static void applyEffects(CompoundTag tag, UpgradeConfigManager.UpgradeConfig config, int level) {
        config.effects.forEach((effect, value) -> {
            switch(effect) {
                case "damage_per_level":
                    tag.putDouble("AttackDamage", 1.0 + (value * level));
                    break;
                case "armor_per_level":
                    tag.putDouble("ArmorToughness", value * level);
                    break;
            }
        });
    }
    // WeaponUpgradeSystem.java에 추가
    public static boolean tryUpgrade(ItemStack weapon, ItemStack stone) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(weapon.getItem());
        Optional<UpgradeConfigManager.ItemConfig> config = UpgradeConfigManager.getItemConfig(itemId);

        if (config.isPresent() && stone.getItem() == ModItems.REINFORCE_STONE.get()) {
            int currentLevel = getCurrentLevel(weapon);
            double successChance = config.get().baseChance - (currentLevel * 0.05);
            boolean success = Math.random() < successChance;

            if(success) {
                applyUpgrade(weapon, true);
                stone.shrink(1);
            }
            return success;
        }
        return false;
    }
}
