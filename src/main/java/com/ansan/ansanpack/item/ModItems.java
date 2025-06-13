package com.ansan.ansanpack.item;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.item.magic.MagicBulletItem;
import com.ansan.ansanpack.item.magic.MagicBulletItem_Admin;
import com.ansan.ansanpack.item.magic.MagicMagazineItem;
import com.ansan.ansanpack.item.magic.ManaBlasterItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.world.item.ArmorItem.Type;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AnsanPack.MODID);

    public static final RegistryObject<Item> ONE_COIN = ITEMS.register("one_coin", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> TEN_COIN = ITEMS.register("ten_coin", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> HUNDRED_COIN = ITEMS.register("hundred_coin", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> THOUSAND_COIN = ITEMS.register("thousand_coin", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> CHUNK_PURCHASE_TICKET = ITEMS.register("chunk_purchase_ticket", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> REINFORCE_STONE = ITEMS.register("reinforce_stone", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> MINOR_HEALING_POTION = ITEMS.register("minor_healing_potion", () -> new CustomPotionItem(new Item.Properties().stacksTo(16), 0.1f, 100, false)); // 5초 쿨타임
    public static final RegistryObject<Item> MEDIUM_HEALING_POTION = ITEMS.register("medium_healing_potion", () -> new CustomPotionItem(new Item.Properties().stacksTo(16), 0.3f, 200, false)); // 10초
    public static final RegistryObject<Item> MAJOR_HEALING_POTION = ITEMS.register("major_healing_potion", () -> new CustomPotionItem(new Item.Properties().stacksTo(16), 0.5f, 300, false)); // 15초
    public static final RegistryObject<Item> SUPER_HEALING_POTION = ITEMS.register("super_healing_potion", () -> new CustomPotionItem(new Item.Properties().stacksTo(16), 0.7f, 400, false)); // 20초
    public static final RegistryObject<Item> HOLY_POTION = ITEMS.register("holy_potion", () -> new CustomPotionItem(new Item.Properties().stacksTo(16), 0.05f, 300, true)); // 디버프 제거 전용
    public static final RegistryObject<Item> RANDOM_PET_BOX = ITEMS.register("random_pet_box", () -> new RandomItemBox(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ETERNITY_STONE = ITEMS.register("eternity_stone", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> OBSIDIAN_HELMET = ITEMS.register("obsidian_helmet", () -> new ArmorItem(new ModArmorMaterial(), ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> OBSIDIAN_CHESTPLATE = ITEMS.register("obsidian_chestplate", () -> new ArmorItem(new ModArmorMaterial(), ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> OBSIDIAN_LEGGINGS = ITEMS.register("obsidian_leggings", () -> new ArmorItem(new ModArmorMaterial(), ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> OBSIDIAN_BOOTS = ITEMS.register("obsidian_boots", () -> new ArmorItem(new ModArmorMaterial(), ArmorItem.Type.BOOTS, new Item.Properties()));

//    public static final RegistryObject<Item> INVENTORY_SAVE_TICKET = ITEMS.register("inventory_save_ticket",() -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> INVENTORY_SAVE_TICKET =
        ITEMS.register("inventory_save_ticket", () -> new Item(new Item.Properties().stacksTo(64)) {
            @Override
            public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
                tooltip.add(Component.translatable("item.ansanpack.inventory_save_ticket.tooltip_1"));
                tooltip.add(Component.translatable("item.ansanpack.inventory_save_ticket.tooltip_2").withStyle(ChatFormatting.GOLD));
            }
        });

    ////Magic
    public static final RegistryObject<Item> MAGIC_BULLET_LOW =
            ITEMS.register("magic_bullet_low", () -> new MagicBulletItem(new Item.Properties()));
    public static final RegistryObject<Item> MAGIC_BULLET_ADMIN =
            ITEMS.register("magic_bullet_admin", () -> new MagicBulletItem_Admin(new Item.Properties()));
    public static final RegistryObject<Item> MAGIC_MAGAZINE =
            ITEMS.register("magic_magazine", () -> new MagicMagazineItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MANA_BLASTER =
            ITEMS.register("mana_blaster", () -> new ManaBlasterItem(new Item.Properties().stacksTo(1)));




}
