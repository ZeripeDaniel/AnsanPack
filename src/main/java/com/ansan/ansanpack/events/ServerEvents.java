package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID)
public class ServerEvents {
    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        if (!server.isDedicatedServer()) return;

        UpgradeConfigManager.loadConfigFromMySQL();
        AnsanPack.LOGGER.info("DB 강화 설정 로드 완료됨");
    }
}
