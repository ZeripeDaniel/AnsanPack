package com.ansan.ansanpack.upgrade;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeChanceManager;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.List;

public class WeaponUpgradeSystem {
    private static final String UPGRADE_TAG = "ansan_upgrade_level";

    public static boolean canUpgrade(ItemStack stack) {
        return UpgradeConfigManager.getConfig(stack.getItem()).isPresent();
    }

    public static double getUpgradeChance(ItemStack stack) {
        int currentLevel = getCurrentLevel(stack);
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return UpgradeChanceManager.getSuccessChance(itemId, currentLevel);
    }

    public static int getCurrentLevel(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        CompoundTag tag = stack.getTag();
        return tag.contains(UPGRADE_TAG) ? tag.getInt(UPGRADE_TAG) : 0;
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
            double total = Math.round(value * level * 100.0) / 100.0;

            switch(effect) {
                case "damage_per_level" -> tag.putDouble("extra_damage", total);
                case "helmet_armor"     -> tag.putDouble("extra_helmet_armor", total);
                case "chest_armor"      -> tag.putDouble("extra_chest_armor", total);
                case "leggings_armor"   -> tag.putDouble("extra_leggings_armor", total);
                case "boots_armor"      -> tag.putDouble("extra_boots_armor", total);
            }
        });
    }

    public static boolean tryUpgrade(ItemStack weapon, ItemStack stone) {
        int currentLevel = getCurrentLevel(weapon);
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(weapon.getItem());

        double successChance = UpgradeChanceManager.getSuccessChance(itemId, currentLevel);
        boolean success = Math.random() < successChance;

        AnsanPack.LOGGER.info("강화 시도 - 확률: {}% → {}", successChance * 100, success ? "성공" : "실패");

        if (success) {
            Optional<UpgradeConfigManager.UpgradeConfig> configOpt = UpgradeConfigManager.getConfig(weapon.getItem());
            if (configOpt.isPresent()) {
                UpgradeConfigManager.UpgradeConfig config = configOpt.get();
                int newLevel = currentLevel + 1;

                CompoundTag tag = weapon.getOrCreateTag();
                tag.putInt(UPGRADE_TAG, newLevel);
                applyEffects(tag, config, newLevel);

                weapon.setTag(tag);
                weapon.setCount(weapon.getCount());

                AnsanPack.LOGGER.info("강화 성공 → 레벨: {}", newLevel);
            }
        }

        return success;
    }

    public static void addUpgradeTooltip(ItemStack stack, List<Component> tooltip) {
        int level = getCurrentLevel(stack);
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        CompoundTag tag = stack.getOrCreateTag();

        if (level > 0) {
            tooltip.add(Component.literal("강화 레벨: +" + level).withStyle(ChatFormatting.GOLD));

            if (tag.contains("extra_damage")) {
                tooltip.add(Component.literal("▶ 추가 공격력: +" + tag.getDouble("extra_damage")).withStyle(ChatFormatting.RED));
            }

            if (stack.getItem() instanceof ArmorItem armor) {
                ArmorItem.Type armorType = armor.getType();
                String armorTagKey = switch (armorType) {
                    case HELMET     -> "extra_helmet_armor";
                    case CHESTPLATE -> "extra_chest_armor";
                    case LEGGINGS   -> "extra_leggings_armor";
                    case BOOTS      -> "extra_boots_armor";
                };
                if (tag.contains(armorTagKey)) {
                    tooltip.add(Component.literal("▶ 추가 방어력: +" + tag.getDouble(armorTagKey)).withStyle(ChatFormatting.BLUE));
                }
            }

            AnsanPack.LOGGER.debug("툴팁 이펙트 값 확인: {} - tag={}", itemId, tag);
        }
    }
}
