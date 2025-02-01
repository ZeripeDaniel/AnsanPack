package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.network.SyncConfigPacket;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID)
public class ServerEvents {
    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("ansanpack_upgrades.json");
        try {
            String jsonData = new String(Files.readAllBytes(configPath));
            // 모든 클라이언트에게 전송
            AnsanPack.NETWORK.send(PacketDistributor.ALL.noArg(), new SyncConfigPacket(jsonData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
