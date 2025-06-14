package com.ansan.ansanpack;

import com.ansan.ansanpack.command.*;
import com.ansan.ansanpack.common.events.AnvilEnchantTransferHandler;
import com.ansan.ansanpack.config.*;
import com.ansan.ansanpack.events.EntityAttributeModifier;
import com.ansan.ansanpack.events.MobDropEventHandler;
import com.ansan.ansanpack.events.UpgradeSystemEventHandler;
import com.ansan.ansanpack.item.magic.ModMagicEntities;
import com.ansan.ansanpack.network.*;
import com.ansan.ansanpack.gui.UpgradeContainer;
import com.ansan.ansanpack.item.ModCreativeTabs;
import com.ansan.ansanpack.item.ModItems;
import com.ansan.ansanpack.skills.ModAttributes;
import com.ansan.ansanpack.sound.ModSoundEvents;
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
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.ansan.ansanpack.gui.UpgradeScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;


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

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);

        // Register items and creative tabs
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        MENUS.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);

        ModMagicEntities.ENTITIES.register(modEventBus); // modEventBus는 FMLJavaModLoadingContext.get().getModEventBus()

        // Register CoinInteractionHandler to the Forge event bus
        MinecraftForge.EVENT_BUS.register(new CoinInteractionHandler());
        MinecraftForge.EVENT_BUS.register(new EntityAttributeModifier());


        MinecraftForge.EVENT_BUS.register(AnvilEnchantTransferHandler.class);
        // 3. 강화 시스템 이벤트 리스너 등록
        MinecraftForge.EVENT_BUS.register(new UpgradeSystemEventHandler());

        MinecraftForge.EVENT_BUS.register(new MobDropEventHandler());
        MinecraftForge.EVENT_BUS.register(this);
        ModSoundEvents.SOUND_EVENTS.register(modEventBus);
    }
    private void setup(final FMLCommonSetupEvent event) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            ConfigManager.loadConfig();
            RandomBoxConfigManager.loadConfig();
            EntityConfigManager.loadConfig();
            UpgradeConfigManager.loadConfigFromMySQL();
            UpgradeChanceManager.loadChancesFromMySQL();
            JobCostManager.loadFromMySQL();
            MobDropManager.loadFromMySQL();
            AnvilRecipeManager.loadFromDatabase();

            MissionManager.load();
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
        UpgradeCommand.register(event.getDispatcher());
        QueryRegisterCommand.register(event.getDispatcher());
        QueryReloadCommand.register(event.getDispatcher());
        JobCommand.register(event.getDispatcher());
        MyInfoCommand.register(event.getDispatcher());
        InfoCommand.register(event.getDispatcher());
        AnvilRecipeRegisterCommand.register(event.getDispatcher());
        MissionCommand.register(event.getDispatcher());
        AddRewardCommand.register(event.getDispatcher());
        JobRemoveCommand.register(event.getDispatcher());
        AddEffectCommand.register(event.getDispatcher());
        CheckEffectsCommand.register(event.getDispatcher());
        RankingCommand.register(event.getDispatcher());
        DebugStatExpCommand.register(event.getDispatcher());
        RefreshUpgradeCommand.register(event.getDispatcher());
    }
    private void commonSetup(final FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {
            int packetId = 0;

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageUpgradeRequest.class,
                    MessageUpgradeRequest::encode,
                    MessageUpgradeRequest::decode,
                    MessageUpgradeRequest::handle);

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageUpgradeResult.class,
                    MessageUpgradeResult::encode,
                    MessageUpgradeResult::decode,
                    MessageUpgradeResult::handle);

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageUpgradeChanceSync.class,
                    MessageUpgradeChanceSync::encode,
                    MessageUpgradeChanceSync::decode,
                    MessageUpgradeChanceSync::handle);

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageOpenMissionUI.class,
                    MessageOpenMissionUI::encode,
                    MessageOpenMissionUI::decode,
                    MessageOpenMissionUI::handle
            );

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageClaimReward.class,
                    MessageClaimReward::encode,
                    MessageClaimReward::decode,
                    MessageClaimReward::handle
            );

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageRewardResult.class,
                    MessageRewardResult::encode,
                    MessageRewardResult::decode,
                    MessageRewardResult::handle
            );

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageSyncMoveDistance.class,
                    MessageSyncMoveDistance::encode,
                    MessageSyncMoveDistance::decode,
                    MessageSyncMoveDistance::handle
            );

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageRequestMissionReset.class,
                    MessageRequestMissionReset::encode,
                    MessageRequestMissionReset::decode,
                    MessageRequestMissionReset::handle
            );

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageInitLevel.class,
                    MessageInitLevel::encode,
                    MessageInitLevel::decode,
                    MessageInitLevel::handle
            );

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageRequestSaveLevel.class,
                    MessageRequestSaveLevel::encode,
                    MessageRequestSaveLevel::decode,
                    MessageRequestSaveLevel::handle
            );

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageGainExp.class,
                    MessageGainExp::encode,
                    MessageGainExp::decode,
                    MessageGainExp::handle
            );
            // 추가된 스탯 패킷 등록
            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageRequestSaveStats.class,
                    MessageRequestSaveStats::encode,
                    MessageRequestSaveStats::decode,
                    MessageRequestSaveStats::handle
            );

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageSyncStats.class,
                    MessageSyncStats::encode,
                    MessageSyncStats::decode,
                    MessageSyncStats::handle
            );
            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageRequestMoneyOnly.class,
                    MessageRequestMoneyOnly::encode,
                    MessageRequestMoneyOnly::decode,
                    MessageRequestMoneyOnly::handle
            );

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageSyncMoneyOnly.class,
                    MessageSyncMoneyOnly::encode,
                    MessageSyncMoneyOnly::decode,
                    MessageSyncMoneyOnly::handle
            );
            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageLevelUpNotify.class,
                    MessageLevelUpNotify::encode,
                    MessageLevelUpNotify::decode,
                    MessageLevelUpNotify::handle
            );
            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageSyncCombatPower.class,
                    MessageSyncCombatPower::encode,
                    MessageSyncCombatPower::decode,
                    MessageSyncCombatPower::handle
            );

            AnsanPack.NETWORK.registerMessage(packetId++,
                    MessageRequestCombatPowerRefresh.class,
                    MessageRequestCombatPowerRefresh::encode,
                    MessageRequestCombatPowerRefresh::decode,
                    MessageRequestCombatPowerRefresh::handle
            );

        });

    }
    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(UPGRADE_CONTAINER.get(), UpgradeScreen::new);

    }
}
