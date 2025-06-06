package com.ansan.ansanpack.item;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AnsanPack.MODID);

    public static final RegistryObject<Item> ONE_COIN = ITEMS.register("one_coin", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> TEN_COIN = ITEMS.register("ten_coin", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> HUNDRED_COIN = ITEMS.register("hundred_coin", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> THOUSAND_COIN = ITEMS.register("thousand_coin", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> CHUNK_PURCHASE_TICKET = ITEMS.register("chunk_purchase_ticket", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> REINFORCE_STONE = ITEMS.register("reinforce_stone", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> MINOR_HEALING_POTION = ITEMS.register("minor_healing_potion", () -> new CustomPotionItem(new Item.Properties().stacksTo(16), 0.1f));
    public static final RegistryObject<Item> MEDIUM_HEALING_POTION = ITEMS.register("medium_healing_potion", () -> new CustomPotionItem(new Item.Properties().stacksTo(16), 0.3f));
    public static final RegistryObject<Item> MAJOR_HEALING_POTION = ITEMS.register("major_healing_potion", () -> new CustomPotionItem(new Item.Properties().stacksTo(16), 0.5f));
    public static final RegistryObject<Item> SUPER_HEALING_POTION = ITEMS.register("super_healing_potion", () -> new CustomPotionItem(new Item.Properties().stacksTo(16), 0.7f));
    public static final RegistryObject<Item> RANDOM_PET_BOX = ITEMS.register("random_pet_box", () -> new RandomItemBox(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ETERNITY_STONE = ITEMS.register("eternity_stone", () -> new Item(new Item.Properties().stacksTo(64)));
}
