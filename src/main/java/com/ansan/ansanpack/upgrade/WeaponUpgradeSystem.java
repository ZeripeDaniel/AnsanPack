package com.ansan.ansanpack.upgrade;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class WeaponUpgradeSystem {
    private static final String UPGRADE_LEVEL_TAG = "upgrade_level";
    private static final float BASE_SUCCESS_CHANCE = 0.8f;
    private static final float CHANCE_DECREASE_PER_LEVEL = 0.05f;

    public static int getUpgradeLevel(ItemStack item) {
        CompoundTag nbt = item.getOrCreateTag();
        return nbt.getInt(UPGRADE_LEVEL_TAG);
    }

    public static void setUpgradeLevel(ItemStack item, int level) {
        CompoundTag nbt = item.getOrCreateTag();
        nbt.putInt(UPGRADE_LEVEL_TAG, level);
    }

    public static boolean tryUpgrade(ItemStack weapon, ItemStack reinforceStone) {
        if (reinforceStone.getItem() != ModItems.REINFORCE_STONE.get()) {
            return false;
        }

        int currentLevel = getUpgradeLevel(weapon);
        float successChance = BASE_SUCCESS_CHANCE - (currentLevel * CHANCE_DECREASE_PER_LEVEL);

        if (Math.random() < successChance) {
            setUpgradeLevel(weapon, currentLevel + 1);
            reinforceStone.shrink(1); // 강화석 소비
            return true;
        } else {
            reinforceStone.shrink(1); // 강화석 소비
            return false;
        }
    }

    public static float getDamageMultiplier(ItemStack item) {
        int level = getUpgradeLevel(item);
        return 1 + (level * 0.1f); // 각 레벨당 10% 데미지 증가
    }
}
