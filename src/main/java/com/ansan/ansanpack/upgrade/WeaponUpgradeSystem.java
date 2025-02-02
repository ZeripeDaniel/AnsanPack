package com.ansan.ansanpack.upgrade;

import com.ansan.ansanpack.AnsanPack;
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
        Optional<UpgradeConfigManager.UpgradeConfig> config = UpgradeConfigManager.getConfig(stack.getItem());
        if (config.isPresent()) {
            int currentLevel = getCurrentLevel(stack);
            // ▼▼▼ 현재 레벨을 반영한 확률 계산 ▼▼▼
            return Math.max(0, config.get().baseChance - (currentLevel * config.get().chanceDecrease));
        }
        return 0;
    }

    public static int getCurrentLevel(ItemStack stack) {
        // ▼▼▼ NBT 존재 여부 체크 강화 ▼▼▼
        if (!stack.hasTag()) return 0;

        CompoundTag tag = stack.getTag();
        assert tag != null;
        return tag.contains("ansan_upgrade_level") ? tag.getInt("ansan_upgrade_level") : 0;
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
        int currentLevel = getCurrentLevel(weapon);

        AnsanPack.LOGGER.info("강화 전 레벨: {}", currentLevel);

        // 강화 성공 여부 계산
        Optional<UpgradeConfigManager.UpgradeConfig> configOpt = UpgradeConfigManager.getConfig(weapon.getItem());
        if (configOpt.isPresent()) {
            UpgradeConfigManager.UpgradeConfig config = configOpt.get();
            double successChance = Math.max(0, config.baseChance - (currentLevel * config.chanceDecrease));
            boolean success = Math.random() < successChance;

            AnsanPack.LOGGER.info("강화 시도 - 성공 확률: {}%, 결과: {}", successChance * 100, success ? "성공" : "실패");

            if (success) {
                CompoundTag tag = weapon.getOrCreateTag();
                tag.putInt("ansan_upgrade_level", currentLevel + 1);
                weapon.setTag(tag);
                weapon.setCount(weapon.getCount()); // 아이템 갱신 트리거
                AnsanPack.LOGGER.info("강화 후 레벨: {}", tag.getInt("ansan_upgrade_level"));
                AnsanPack.LOGGER.info("NBT 저장 확인: {}", tag); // 로깅 추가
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
