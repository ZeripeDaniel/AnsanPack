package com.ansan.ansanpack.item;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.item.magic.MagicBulletItem;
import com.ansan.ansanpack.item.magic.MagicBulletItem_Admin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.core.registries.Registries;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AnsanPack.MODID);

    public static final RegistryObject<CreativeModeTab> ANSAN_TAB = CREATIVE_MODE_TABS.register("ansan_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ONE_COIN.get())) // 탭 아이콘
                    .title(Component.translatable("itemGroup.ansanpack.currency")) // 탭 이름
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ONE_COIN.get());
                        output.accept(ModItems.TEN_COIN.get());
                        output.accept(ModItems.HUNDRED_COIN.get());
                        output.accept(ModItems.THOUSAND_COIN.get());
                        output.accept(ModItems.CHUNK_PURCHASE_TICKET.get());
                        output.accept(ModItems.REINFORCE_STONE.get());
                        output.accept(ModItems.ETERNITY_STONE.get());
                        output.accept(ModItems.MINOR_HEALING_POTION.get());
                        output.accept(ModItems.MEDIUM_HEALING_POTION.get());
                        output.accept(ModItems.MAJOR_HEALING_POTION.get());
                        output.accept(ModItems.SUPER_HEALING_POTION.get());
                        output.accept(ModItems.RANDOM_PET_BOX.get());
                        output.accept(ModItems.OBSIDIAN_HELMET.get());
                        output.accept(ModItems.OBSIDIAN_BOOTS.get());
                        output.accept(ModItems.OBSIDIAN_CHESTPLATE.get());
                        output.accept(ModItems.OBSIDIAN_LEGGINGS.get());
                        output.accept(ModItems.INVENTORY_SAVE_TICKET.get());
                        output.accept(ModItems.MANA_BLASTER.get());
                        output.accept(ModItems.MAGIC_MAGAZINE.get().getDefaultInstance());
                        output.accept(MagicBulletItem.createBullet(3.5f));
                        output.accept(MagicBulletItem_Admin.createBullet(9999f));
                    })
                    .build());
}
