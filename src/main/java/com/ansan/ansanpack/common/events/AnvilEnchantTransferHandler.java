package com.ansan.ansanpack.common.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.ConfigManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnvilEnchantTransferHandler {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        String name = event.getName();

        for (ConfigManager.AnvilRecipe recipe : ConfigManager.getRecipes()) {
            if (left.getItem() == recipe.getInsertItem() &&
                    right.getItem() == recipe.getResourceItem() &&
                    right.getCount() >= recipe.stack) {

                ItemStack newItem = new ItemStack(recipe.getResultItem());

                // 인챈트 전송
                EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(left), newItem);

                if (!name.isEmpty()) {
                    newItem.setHoverName(Component.literal(name));
                }

                // 결과 설정
                event.setOutput(newItem);
                event.setCost(recipe.costLevel); // 경험치 비용 설정
                event.setMaterialCost(recipe.stack); // 재료 비용 설정
                return;
            }
        }
    }
}
