package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import com.ansan.ansanpack.config.StatDatabaseManager;
import com.ansan.ansanpack.network.MessageSyncStats;
import com.ansan.ansanpack.server.stat.PlayerStat;
import com.ansan.ansanpack.server.stat.ServerStatCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StatLoginSyncHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // ✅ DB에서 불러오기 (없으면 기본값으로 채워짐)
        PlayerStat stat = StatDatabaseManager.loadStats(player.getUUID());

        // ✅ 서버 캐시에 저장
        ServerStatCache.update(player.getUUID(), stat);

        // ✅ 클라이언트로 동기화
        AnsanPack.NETWORK.sendTo(
                new MessageSyncStats(
                        stat.getStrength(),
                        stat.getAgility(),
                        stat.getIntelligence(),
                        stat.getLuck(),
                        stat.getAvailableAP()
                ),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }


}
