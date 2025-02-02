package com.ansan.ansanpack;

import com.ansan.ansanpack.command.*;
import com.ansan.ansanpack.common.events.AnvilEnchantTransferHandler;
import com.ansan.ansanpack.config.ConfigManager;
import com.ansan.ansanpack.config.EntityConfigManager;
import com.ansan.ansanpack.config.RandomBoxConfigManager;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.events.EntityAttributeModifier;
import com.ansan.ansanpack.events.UpgradeSystemEventHandler;
import com.ansan.ansanpack.network.*;
import com.ansan.ansanpack.gui.UpgradeContainer;
import com.ansan.ansanpack.item.ModCreativeTabs;
import com.ansan.ansanpack.item.ModItems;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.ansan.ansanpack.gui.UpgradeScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(AnsanPack.MODID)
public class AnsanPack {
    public static final String MODID = "ansanpack";
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final RegistryObject<MenuType<UpgradeContainer>> UPGRADE_CONTAINER =
            MENUS.register("upgrade_container", () -> IForgeMenuType.create((windowId, inv, data) -> new UpgradeContainer(windowId, inv)));

    // 1. 네트워크 채널 추가 (클래스 상단에 추가)
    // 수정 코드 (Forge 47.3.x 대응)
    public static final net.minecraftforge.network.simple.SimpleChannel NETWORK =
            net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(MODID, "main"),
                    () -> "1.0",
                    s -> true,
                    s -> true
            );

    public static final Logger LOGGER = LogManager.getLogger();

    public AnsanPack() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);

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

        // 3. 강화 시스템 이벤트 리스너 등록
        MinecraftForge.EVENT_BUS.register(new UpgradeSystemEventHandler());
    }
    private void setup(final FMLCommonSetupEvent event) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            ConfigManager.loadConfig();
            RandomBoxConfigManager.loadConfig();
            EntityConfigManager.loadConfig();
            UpgradeConfigManager.loadConfig();
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        DepositCommand.register(event.getDispatcher());
        WithdrawCommand.register(event.getDispatcher());
        BalanceCommand.register(event.getDispatcher());
        GiveMoneyCommand.register(event.getDispatcher());
        TransferCommand.register(event.getDispatcher());
        PetCommand.register(event.getDispatcher());
        UpgradeCommand.register(event.getDispatcher()); // 새로운 명령어 등록
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        // 공통 설정
        // 4. 네트워크 패킷 등록
        int packetId = 0;
        NETWORK.registerMessage(packetId++,
                SyncConfigPacket.class,
                SyncConfigPacket::encode,
                SyncConfigPacket::decode,
                SyncConfigPacket::handle);

        NETWORK.registerMessage(packetId++,
                MessageUpgradeRequest.class,
                MessageUpgradeRequest::encode,
                MessageUpgradeRequest::decode,
                MessageUpgradeRequest::handle);

        NETWORK.registerMessage(packetId++,
                MessageUpgradeResult.class,
                MessageUpgradeResult::encode,
                MessageUpgradeResult::decode,
                MessageUpgradeResult::handle);
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(UPGRADE_CONTAINER.get(), UpgradeScreen::new);
    }

}
