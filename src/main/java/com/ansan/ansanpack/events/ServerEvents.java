package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.network.SyncConfigPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID)
public class ServerEvents {
    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        if (!server.isDedicatedServer()) return; // LAN 서버 방지

        Path configPath = server.getServerDirectory().toPath()
                .resolve("config").resolve("ansanpack_upgrades.json");

        try {
            String jsonData = new String(Files.readAllBytes(configPath));
            AnsanPack.NETWORK.send(
                    PacketDistributor.ALL.noArg(),
                    new SyncConfigPacket(jsonData)
            );
        } catch (IOException e) {
            AnsanPack.LOGGER.error("Failed to load upgrade config!", e);
        }
    }
}
