package com.ansan.ansanpack;

import com.ansan.ansanpack.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CoinInteractionHandler {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        if (item.isEmpty()) {
            return;
        }

        if (item.getItem() == ModItems.ONE_COIN.get() ||
                item.getItem() == ModItems.TEN_COIN.get() ||
                item.getItem() == ModItems.HUNDRED_COIN.get() ||
                item.getItem() == ModItems.THOUSAND_COIN.get()) {

            if (player.isShiftKeyDown()) {
                ItemStack lowerTier = getLowerTierItem(item);
                if (!lowerTier.isEmpty()) {
                    item.shrink(1);
                    if (!player.getInventory().add(lowerTier)) {
                        player.drop(lowerTier, false);
                    }
                    event.setCanceled(true);
                }
            } else if (item.getCount() >= 10) {
                ItemStack higherTier = getHigherTierItem(item);
                if (!higherTier.isEmpty()) {
                    item.shrink(10);
                    if (!player.getInventory().add(higherTier)) {
                        player.drop(higherTier, false);
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

    private static ItemStack getLowerTierItem(ItemStack item) {
        if (item.getItem() == ModItems.TEN_COIN.get()) {
            return new ItemStack(ModItems.ONE_COIN.get(), 10);
        } else if (item.getItem() == ModItems.HUNDRED_COIN.get()) {
            return new ItemStack(ModItems.TEN_COIN.get(), 10);
        } else if (item.getItem() == ModItems.THOUSAND_COIN.get()) {
            return new ItemStack(ModItems.HUNDRED_COIN.get(), 10);
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack getHigherTierItem(ItemStack item) {
        if (item.getItem() == ModItems.ONE_COIN.get()) {
            return new ItemStack(ModItems.TEN_COIN.get(), 1);
        } else if (item.getItem() == ModItems.TEN_COIN.get()) {
            return new ItemStack(ModItems.HUNDRED_COIN.get(), 1);
        } else if (item.getItem() == ModItems.HUNDRED_COIN.get()) {
            return new ItemStack(ModItems.THOUSAND_COIN.get(), 1);
        }
        return ItemStack.EMPTY;
    }
}
