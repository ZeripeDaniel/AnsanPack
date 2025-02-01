package com.ansan.ansanpack.upgrade;

import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.List;

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
        Optional<UpgradeConfigManager.UpgradeConfig> config = UpgradeConfigManager.getConfig(weapon.getItem());

        if (config.isPresent() && stone.getItem() == ModItems.REINFORCE_STONE.get()) {
            int currentLevel = getCurrentLevel(weapon);
            if (currentLevel >= config.get().maxLevel) return false;
            double successChance = config.get().baseChance - (currentLevel * config.get().chanceDecrease);
            boolean success = Math.random() < successChance;

            stone.shrink(1);

            if(success) {
                applyUpgrade(weapon, true);

            }
            return success;
        }
        return false;
    }

    
    public static void addUpgradeTooltip(ItemStack stack, List<Component> tooltip) {
        int level = getCurrentLevel(stack);
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (level > 0) {
            tooltip.add(Component.literal("강화 레벨: +" + level).withStyle(ChatFormatting.GOLD));

            double harmorValue = UpgradeConfigManager.getEffectValue(itemId, "helmet_armor");
            double carmorValue = UpgradeConfigManager.getEffectValue(itemId, "chest_armor");
            double larmorValue = UpgradeConfigManager.getEffectValue(itemId, "leggings_armor");
            double barmorValue = UpgradeConfigManager.getEffectValue(itemId, "boots_armor");
            double damageValue = UpgradeConfigManager.getEffectValue(itemId, "damage_per_level");
            if (stack.getItem() instanceof ArmorItem) {
                ArmorItem armor = (ArmorItem) stack.getItem();
                ArmorItem.Type armorType = armor.getType();
                switch (armorType) {
                    case HELMET:
                        tooltip.add(Component.literal("▶ 추가 방어력: +" + (level * harmorValue)).withStyle(ChatFormatting.BLUE));
                        break;
                    case CHESTPLATE:
                        tooltip.add(Component.literal("▶ 추가 방어력: +" + (level * carmorValue)).withStyle(ChatFormatting.BLUE));
                        break;
                    case LEGGINGS:
                        tooltip.add(Component.literal("▶ 추가 방어력: +" + (level * larmorValue)).withStyle(ChatFormatting.BLUE));
                        break;
                    case BOOTS:
                        tooltip.add(Component.literal("▶ 추가 방어력: +" + (level * barmorValue)).withStyle(ChatFormatting.BLUE));
                        break;
                }
            } else if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof ProjectileWeaponItem) {
                tooltip.add(Component.literal("▶ 추가 공격력: +" + (level * damageValue)).withStyle(ChatFormatting.RED));
            }
        }
    }


}
