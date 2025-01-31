package com.ansan.ansanpack;

import com.ansan.ansanpack.command.*;
import com.ansan.ansanpack.common.events.AnvilEnchantTransferHandler;
import com.ansan.ansanpack.config.ConfigManager;
import com.ansan.ansanpack.config.EntityConfigManager;
import com.ansan.ansanpack.config.RandomBoxConfigManager;
import com.ansan.ansanpack.events.EntityAttributeModifier;
import com.ansan.ansanpack.gui.UpgradeContainer;
import com.ansan.ansanpack.item.ModCreativeTabs;
import com.ansan.ansanpack.item.ModItems;
import com.ansan.ansanpack.item.RandomItemBox;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.ansan.ansanpack.gui.UpgradeScreen;


@Mod(AnsanPack.MODID)
public class AnsanPack {
    public static final String MODID = "ansanpack";
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final RegistryObject<MenuType<UpgradeContainer>> UPGRADE_CONTAINER =
            MENUS.register("upgrade_container", () -> IForgeMenuType.create((windowId, inv, data) -> new UpgradeContainer(windowId, inv)));
    public AnsanPack() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        EntityConfigManager.loadConfig();

        // Register items and creative tabs
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // Register CoinInteractionHandler to the Forge event bus
        MinecraftForge.EVENT_BUS.register(new CoinInteractionHandler());
        MinecraftForge.EVENT_BUS.register(new EntityAttributeModifier());

        // Register commands
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(AnvilEnchantTransferHandler.class);

        MENUS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);


    }
    private void setup(final FMLCommonSetupEvent event) {
        ConfigManager.loadConfig();
        RandomBoxConfigManager.loadConfig();
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        DepositCommand.register(event.getDispatcher());
        WithdrawCommand.register(event.getDispatcher());
        BalanceCommand.register(event.getDispatcher());
        GiveMoneyCommand.register(event.getDispatcher());
        TransferCommand.register(event.getDispatcher());
        PetCommand.register(event.getDispatcher());
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        // 공통 설정
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(UPGRADE_CONTAINER.get(), UpgradeScreen::new);

    }
}
