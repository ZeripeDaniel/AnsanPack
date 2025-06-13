package com.ansan.ansanpack.upgrade;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeChanceManager;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.events.MissionEventDispatcher;
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

    public static void applyEffects(CompoundTag tag, UpgradeConfigManager.UpgradeConfig config, int level) {
        config.effects.forEach((effectKey, effectList) -> {
            for (UpgradeConfigManager.EffectEntry entry : effectList) {
                 if (level >= entry.applyLevel) {
                     double total = 0.0;
                     for (int lv = entry.applyLevel; lv <= level; lv++) {
                         int fakeLevel = lv - entry.applyLevel + 1;
                         total += entry.value * getEffectMultiplier(fakeLevel);
                     }
                     double rounded = Math.round(total * 100.0) / 100.0;

                    switch (effectKey) {
                        case "damage_per_level"       -> tag.putDouble("extra_damage", rounded);
                        case "attack_spd_level"       -> tag.putDouble("extra_attack_speed", rounded);
                        case "knockback_level"        -> tag.putDouble("extra_knockback", rounded);
                        case "helmet_armor"           -> tag.putDouble("extra_helmet_armor", rounded);
                        case "chest_armor"            -> tag.putDouble("extra_chest_armor", rounded);
                        case "leggings_armor"         -> tag.putDouble("extra_leggings_armor", rounded);
                        case "boots_armor"            -> tag.putDouble("extra_boots_armor", rounded);
                        case "health_bonus"           -> tag.putDouble("extra_health", rounded);
                        case "resist_knockback"       -> tag.putDouble("extra_knockback_resistance", rounded);
                        case "toughness_bonus"        -> tag.putDouble("extra_toughness", rounded);
                        case "move_speed_bonus"       -> tag.putDouble("extra_move_speed", rounded);
                        case "luck_bonus"             -> tag.putDouble("extra_luck", rounded);
                    }
                    break;
                }
            }
        });
    }


    public static double getEffectMultiplier(int level) {
        if (level == 15) return 4.5;
        if (level == 14) return 3.5;
        if (level == 13) return 3.2;
        if (level >= 11) return 3.0;  // 11~13강
        if (level >= 6) return 2.0;    // 6~10강
        return 1.0;                    // 1~5강
    }

    public static boolean tryUpgrade(ItemStack weapon, ItemStack stone) {
        int currentLevel = getCurrentLevel(weapon);
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(weapon.getItem());

        // 🔐 max_level 체크
        Optional<UpgradeConfigManager.UpgradeConfig> configOpt = UpgradeConfigManager.getConfig(weapon.getItem());
        if (configOpt.isEmpty()) return false;

        UpgradeConfigManager.UpgradeConfig config = configOpt.get();

        if (currentLevel >= config.maxLevel) {
            AnsanPack.LOGGER.debug("강화 불가: {}는 최대 강화 레벨 {}에 도달함", itemId, config.maxLevel);
            return false;
        }

        double successChance = UpgradeChanceManager.getSuccessChance(itemId, currentLevel);
        boolean success = Math.random() < successChance;

        AnsanPack.LOGGER.debug("강화 시도 - 확률: {}% → {}", successChance * 100, success ? "성공" : "실패");


        if (success) {
            int newLevel = currentLevel + 1;

            CompoundTag tag = weapon.getOrCreateTag();
            tag.putInt(UPGRADE_TAG, newLevel);
            applyEffects(tag, config, newLevel);

            weapon.setTag(tag);
            weapon.setCount(weapon.getCount());

            AnsanPack.LOGGER.info("강화 성공 → 레벨: {}", newLevel);
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
                tooltip.add(Component.literal("▶ 추가 공격력: +" + tag.getDouble("extra_damage"))
                        .withStyle(ChatFormatting.RED));
            }

            if (tag.contains("extra_attack_speed")) {
                tooltip.add(Component.literal("▶ 추가 공격 속도: +" + tag.getDouble("extra_attack_speed"))
                        .withStyle(ChatFormatting.YELLOW));
            }

            if (tag.contains("extra_knockback")) {
                tooltip.add(Component.literal("▶ 넉백 증가: +" + tag.getDouble("extra_knockback"))
                        .withStyle(ChatFormatting.DARK_PURPLE));
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
                    tooltip.add(Component.literal("▶ 추가 방어력: +" + tag.getDouble(armorTagKey))
                            .withStyle(ChatFormatting.BLUE));
                }
            }

            if (tag.contains("extra_health")) {
                tooltip.add(Component.literal("▶ 체력 보너스: +" + tag.getDouble("extra_health"))
                        .withStyle(ChatFormatting.DARK_RED));
            }

            if (tag.contains("extra_knockback_resistance")) {
                tooltip.add(Component.literal("▶ 넉백 저항: +" + tag.getDouble("extra_knockback_resistance"))
                        .withStyle(ChatFormatting.GRAY));
            }

            if (tag.contains("extra_toughness")) {
                tooltip.add(Component.literal("▶ 방어 강도: +" + tag.getDouble("extra_toughness"))
                        .withStyle(ChatFormatting.DARK_AQUA));
            }

            if (tag.contains("extra_move_speed")) {
                double raw = tag.getDouble("extra_move_speed");
                double shown = Math.round(raw * 10000.0 * 100.0) / 100.0; // 소수점 둘째 자리까지
                tooltip.add(Component.literal("▶ 이동 속도 증가: +" + shown)
                        .withStyle(ChatFormatting.GREEN));
            }

            if (tag.contains("extra_luck")) {
                tooltip.add(Component.literal("▶ 행운: +" + tag.getDouble("extra_luck"))
                        .withStyle(ChatFormatting.GOLD));
            }

            AnsanPack.LOGGER.debug("툴팁 이펙트 값 확인: {} - tag={}", itemId, tag);
        }
    }

//    public static void addUpgradeTooltip(ItemStack stack, List<Component> tooltip) {
//        int level = getCurrentLevel(stack);
//        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
//        CompoundTag tag = stack.getOrCreateTag();
//
//        if (level > 0) {
//            tooltip.add(Component.literal("강화 레벨: +" + level).withStyle(ChatFormatting.GOLD));
//
//            if (tag.contains("extra_damage")) {
//                tooltip.add(Component.literal("▶ 추가 공격력: +" + tag.getDouble("extra_damage")).withStyle(ChatFormatting.RED));
//            }
//
//            if (stack.getItem() instanceof ArmorItem armor) {
//                ArmorItem.Type armorType = armor.getType();
//                String armorTagKey = switch (armorType) {
//                    case HELMET     -> "extra_helmet_armor";
//                    case CHESTPLATE -> "extra_chest_armor";
//                    case LEGGINGS   -> "extra_leggings_armor";
//                    case BOOTS      -> "extra_boots_armor";
//                };
//                if (tag.contains(armorTagKey)) {
//                    tooltip.add(Component.literal("▶ 추가 방어력: +" + tag.getDouble(armorTagKey)).withStyle(ChatFormatting.BLUE));
//                }
//            }
//
//            AnsanPack.LOGGER.debug("툴팁 이펙트 값 확인: {} - tag={}", itemId, tag);
//        }
//    }
}
