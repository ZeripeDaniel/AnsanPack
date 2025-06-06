package com.ansan.ansanpack.common.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.AnvilRecipeManager;
import com.ansan.ansanpack.item.ModItems;  // 영원의 돌 참조
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnvilEnchantTransferHandler {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        String name = event.getName();

        // 영원의 돌과 조합 시 내구도 무한화 처리
        if (right.getItem() == ModItems.ETERNITY_STONE.get()) {
            // 왼쪽 아이템에 NBT가 존재하면 복사
            if (left.hasTag()) {
                ItemStack newItem = left.copy();  // 기존 아이템 복사
                CompoundTag oldTag = left.getTag();
                CompoundTag newTag = new CompoundTag();

                // 기존 NBT 값 복사
                for (String key : oldTag.getAllKeys()) {
                    newTag.put(key, oldTag.get(key).copy());
                }

                // 내구도 무한화 적용
                newTag.putBoolean("Unbreakable", true);

                // 새로운 아이템에 NBT 설정
                newItem.setTag(newTag);

                // 인챈트 복사
                EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(left), newItem);

                // 이름 설정
                if (!name.isEmpty()) {
                    newItem.setHoverName(Component.literal(name));
                }

                // 결과 설정
                event.setOutput(newItem);
                event.setCost(1);  // 경험치 비용 설정 (1로 설정, 필요시 조정 가능)
                event.setMaterialCost(1); // 재료 비용 설정 (영원의 돌 1개)
                return;
            }
        }

        // 기존 레시피 처리 (기존 코드 그대로 유지)
        for (AnvilRecipeManager.AnvilRecipe recipe : AnvilRecipeManager.getRecipes()) {
            if (left.getItem() == recipe.insertItem() &&
                    right.getItem() == recipe.resourceItem() &&
                    right.getCount() >= recipe.stack()) {

                ItemStack newItem = new ItemStack(recipe.resultItem());

                // 기존 NBT 복사
                if (left.hasTag()) {
                    CompoundTag oldTag = left.getTag();
                    CompoundTag newTag = new CompoundTag();

                    String[] keysToCopy = {
                            "ansan_upgrade_level", "extra_damage",
                            "extra_helmet_armor", "extra_chest_armor",
                            "extra_leggings_armor", "extra_boots_armor"
                    };

                    for (String key : keysToCopy) {
                        if (oldTag.contains(key)) {
                            newTag.put(key, oldTag.get(key).copy());
                        }
                    }

                    newItem.setTag(newTag);
                }

                // 인챈트 복사
                EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(left), newItem);

                // 이름 적용
                if (!name.isEmpty()) {
                    newItem.setHoverName(Component.literal(name));
                }

                // 결과 설정
                event.setOutput(newItem);
                event.setCost(recipe.costLevel());
                event.setMaterialCost(recipe.stack());
                return;
            }
        }
    }
}
