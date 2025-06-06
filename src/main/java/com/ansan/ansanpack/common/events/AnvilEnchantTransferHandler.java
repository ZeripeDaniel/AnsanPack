package com.ansan.ansanpack.common.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.AnvilRecipeManager;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnvilEnchantTransferHandler {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        String name = event.getName();

        // 1. 영원의 돌 조합 처리
        if (right.getItem() == ModItems.ETERNITY_STONE.get()) {
            if (left.hasTag()) {
                ItemStack newItem = left.copy();
                CompoundTag oldTag = left.getTag();
                CompoundTag newTag = new CompoundTag();

                for (String key : oldTag.getAllKeys()) {
                    newTag.put(key, oldTag.get(key).copy());
                }

                newTag.putBoolean("Unbreakable", true);
                newItem.setTag(newTag);
                EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(left), newItem);

                if (!name.isEmpty()) {
                    newItem.setHoverName(Component.literal(name));
                }

                event.setOutput(newItem);
                event.setCost(1);
                event.setMaterialCost(1);
                return;
            }
        }

        // 2. 커스텀 모루 조합 처리
        for (AnvilRecipeManager.AnvilRecipe recipe : AnvilRecipeManager.getRecipes()) {
            if (left.getItem() == recipe.insertItem() &&
                    right.getItem() == recipe.resourceItem() &&
                    right.getCount() >= recipe.stack()) {

                ItemStack newItem = new ItemStack(recipe.resultItem());
                CompoundTag newTag = new CompoundTag();
                CompoundTag oldTag = left.getTag();

                int newUpgradeLevel = 0;

                if (oldTag != null && oldTag.contains("ansan_upgrade_level")) {
                    int oldLevel = oldTag.getInt("ansan_upgrade_level");

                    if (recipe.isTierUpgrade()) {
                        // 티어 업그레이드: 강화 레벨 절반, 이펙트 재적용
                        newUpgradeLevel = Math.max(0, oldLevel / 2);
                        newTag.putInt("ansan_upgrade_level", newUpgradeLevel);
                        applyTierUpgradeEffects(recipe.resultItem(), newTag, newUpgradeLevel);

                    } else {
                        // 일반 조합: 강화 관련 NBT만 복사
                        newTag.putInt("ansan_upgrade_level", oldLevel);
                        copyExtraNBT(oldTag, newTag);
                    }
                }

                newItem.setTag(newTag);
                EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(left), newItem);

                if (!name.isEmpty()) {
                    newItem.setHoverName(Component.literal(name));
                }

                event.setOutput(newItem);
                event.setCost(recipe.costLevel());
                event.setMaterialCost(recipe.stack());
                return;
            }
        }
    }

    private static void copyExtraNBT(CompoundTag from, CompoundTag to) {
        String[] keys = {
                "extra_damage",
                "extra_attack_speed",
                "extra_knockback",
                "extra_helmet_armor", "extra_chest_armor",
                "extra_leggings_armor", "extra_boots_armor",
                "extra_health", "extra_knockback_resistance",
                "extra_toughness", "extra_move_speed", "extra_luck"
        };

        for (String key : keys) {
            if (from.contains(key)) {
                to.put(key, from.get(key).copy());
            }
        }
    }


    private static void applyTierUpgradeEffects(net.minecraft.world.item.Item resultItem, CompoundTag tag, int level) {
        UpgradeConfigManager.getConfig(resultItem).ifPresent(config -> {
            config.effects.forEach((effect, value) -> {
                double scaled = Math.round(value * level * 100.0) / 100.0;

                switch (effect) {
                    case "damage_per_level"       -> tag.putDouble("extra_damage", scaled);
                    case "attack_spd_level"       -> tag.putDouble("extra_attack_speed", scaled);
                    case "knockback_level"        -> tag.putDouble("extra_knockback", scaled);
                    case "helmet_armor"           -> tag.putDouble("extra_helmet_armor", scaled);
                    case "chest_armor"            -> tag.putDouble("extra_chest_armor", scaled);
                    case "leggings_armor"         -> tag.putDouble("extra_leggings_armor", scaled);
                    case "boots_armor"            -> tag.putDouble("extra_boots_armor", scaled);
                    case "health_bonus"           -> tag.putDouble("extra_health", scaled);
                    case "resist_knockback"       -> tag.putDouble("extra_knockback_resistance", scaled);
                    case "toughness_bonus"        -> tag.putDouble("extra_toughness", scaled);
                    case "move_speed_bonus"       -> tag.putDouble("extra_move_speed", scaled);
                    case "luck_bonus"             -> tag.putDouble("extra_luck", scaled);
                }
            });
        });
    }

}
