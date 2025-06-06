package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import com.ansan.ansanpack.config.StatDatabaseManager;
import com.ansan.ansanpack.network.MessageSyncStats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = "ansanpack", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StatLoginSyncHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        StatDatabaseManager.loadStats(player.getUUID());

        int str = LocalPlayerStatData.INSTANCE.getStat("str");
        int agi = LocalPlayerStatData.INSTANCE.getStat("agi");
        int intel = LocalPlayerStatData.INSTANCE.getStat("int");
        int luck = LocalPlayerStatData.INSTANCE.getStat("luck");
        int ap = LocalPlayerStatData.INSTANCE.getAvailableAP();

        AnsanPack.NETWORK.sendTo(
                new MessageSyncStats(str, agi, intel, luck, ap),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }
}
