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
    private static final String UPGRADE_TAG = "ansan_upgrade_level";

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
                case "damage_per_level" -> {
                    tag.putDouble("extra_damage", total); // 툴팁 표시용
                }
                case "helmet_armor" -> tag.putDouble("extra_helmet_armor", total);
                case "chest_armor" -> tag.putDouble("extra_chest_armor", total);
                case "leggings_armor" -> tag.putDouble("extra_leggings_armor", total);
                case "boots_armor" -> tag.putDouble("extra_boots_armor", total);
            }
        });
    }


    // WeaponUpgradeSystem.java에 추가
//    public static boolean tryUpgrade(ItemStack weapon, ItemStack stone) {
//        int currentLevel = getCurrentLevel(weapon);
//
//        AnsanPack.LOGGER.info("강화 전 레벨: {}", currentLevel);
//
//        // 강화 성공 여부 계산
//        Optional<UpgradeConfigManager.UpgradeConfig> configOpt = UpgradeConfigManager.getConfig(weapon.getItem());
//        if (configOpt.isPresent()) {
//            UpgradeConfigManager.UpgradeConfig config = configOpt.get();
//            double successChance = Math.max(0, config.baseChance - (currentLevel * config.chanceDecrease));
//            boolean success = Math.random() < successChance;
//
//            AnsanPack.LOGGER.info("강화 시도 - 성공 확률: {}%, 결과: {}", successChance * 100, success ? "성공" : "실패");
//
//            if (success) {
//                CompoundTag tag = weapon.getOrCreateTag();
//                int newLevel = currentLevel + 1;
//                tag.putInt(UPGRADE_TAG, newLevel);
//
//                applyEffects(tag, config, newLevel);
//
//                weapon.setTag(tag);
//                weapon.setCount(weapon.getCount()); // 아이템 갱신 트리거
//                AnsanPack.LOGGER.info("강화 후 레벨: {}", tag.getInt(UPGRADE_TAG));
//                AnsanPack.LOGGER.info("NBT 저장 확인: {}", tag); // 로깅 추가
//            }
//            return success;
//        }
//        return false;
//    }
    public static boolean tryUpgrade(ItemStack weapon, ItemStack stone) {
        int currentLevel = getCurrentLevel(weapon);

        AnsanPack.LOGGER.info("강화 전 레벨: {}", currentLevel);

        Optional<UpgradeConfigManager.UpgradeConfig> configOpt = UpgradeConfigManager.getConfig(weapon.getItem());
        if (configOpt.isPresent()) {
            UpgradeConfigManager.UpgradeConfig config = configOpt.get();
            double successChance = Math.max(0, config.baseChance - (currentLevel * config.chanceDecrease));
            boolean success = Math.random() < successChance;

            AnsanPack.LOGGER.info("강화 시도 - 성공 확률: {}%, 결과: {}", successChance * 100, success ? "성공" : "실패");

            if (success) {
                // ✅ 완전한 태그 적용 흐름
                ItemStack copy = weapon.copy();
                CompoundTag tag = copy.getOrCreateTag();

                int newLevel = currentLevel + 1;
                tag.putInt(UPGRADE_TAG, newLevel);

                // 🔥 효과 적용
                applyEffects(tag, config, newLevel);

                // ✅ 복사한 태그로 원본을 대체
                weapon.setTag(tag);
                weapon.setCount(weapon.getCount()); // 갱신 트리거

                AnsanPack.LOGGER.info("강화 후 레벨: {}", tag.getInt(UPGRADE_TAG));
                AnsanPack.LOGGER.info("NBT 저장 확인: {}", tag);
            }

            return success;
        }

        return false;
    }

//    public static void addUpgradeTooltip(ItemStack stack, List<Component> tooltip) {
//        int level = getCurrentLevel(stack);
//        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
//        if (level > 0) {
//            tooltip.add(Component.literal("강화 레벨: +" + level).withStyle(ChatFormatting.GOLD));
//
//            double damageValue = UpgradeConfigManager.getAnyEffectValue(itemId, "damage_per_level");
//
//            double harmorValue = UpgradeConfigManager.getAnyEffectValue(itemId, "helmet_armor");
//            double carmorValue = UpgradeConfigManager.getAnyEffectValue(itemId, "chest_armor");
//            double larmorValue = UpgradeConfigManager.getAnyEffectValue(itemId, "leggings_armor");
//            double barmorValue = UpgradeConfigManager.getAnyEffectValue(itemId, "boots_armor");
//            if (stack.getItem() instanceof ArmorItem) {
//                ArmorItem armor = (ArmorItem) stack.getItem();
//                ArmorItem.Type armorType = armor.getType();
//                switch (armorType) {
//                    case HELMET:
//                        tooltip.add(Component.literal("▶ 추가 방어력: +" + (level * harmorValue)).withStyle(ChatFormatting.BLUE));
//                        break;
//                    case CHESTPLATE:
//                        tooltip.add(Component.literal("▶ 추가 방어력: +" + (level * carmorValue)).withStyle(ChatFormatting.BLUE));
//                        break;
//                    case LEGGINGS:
//                        tooltip.add(Component.literal("▶ 추가 방어력: +" + (level * larmorValue)).withStyle(ChatFormatting.BLUE));
//                        break;
//                    case BOOTS:
//                        tooltip.add(Component.literal("▶ 추가 방어력: +" + (level * barmorValue)).withStyle(ChatFormatting.BLUE));
//                        break;
//                }
//            } else if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof ProjectileWeaponItem) {
//                tooltip.add(Component.literal("▶ 추가 공격력: +" + (level * damageValue)).withStyle(ChatFormatting.RED));
//            }
//            AnsanPack.LOGGER.debug("툴팁 이펙트 값 확인: {} - damageValue={}", itemId, damageValue);
//
//        }
//    }
public static void addUpgradeTooltip(ItemStack stack, List<Component> tooltip) {
    int level = getCurrentLevel(stack);
    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
    CompoundTag tag = stack.getOrCreateTag();

    if (level > 0) {
        tooltip.add(Component.literal("강화 레벨: +" + level).withStyle(ChatFormatting.GOLD));

        // ▶ 추가 공격력 (무기)
        if (tag.contains("extra_damage")) {
            tooltip.add(Component.literal("▶ 추가 공격력: +" + tag.getDouble("extra_damage")).withStyle(ChatFormatting.RED));
        }

        // ▶ 방어력 (방어구)
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

        // 디버깅 로그
        AnsanPack.LOGGER.debug("툴팁 이펙트 값 확인: {} - tag={}", itemId, tag);
    }
}


}
