package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.StatDatabaseManager;
import com.ansan.ansanpack.network.MessageSyncCombatPower;
import com.ansan.ansanpack.network.MessageSyncStats;
import com.ansan.ansanpack.server.stat.PlayerStat;
import com.ansan.ansanpack.server.stat.ServerStatCache;
import com.ansan.ansanpack.common.CombatPowerCalculator;
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

        PlayerStat stat = StatDatabaseManager.loadStats(player.getUUID());
        ServerStatCache.update(player.getUUID(), stat);
        if (stat == null) {
            stat = new PlayerStat(); // 기본값
            AnsanPack.LOGGER.warn("[StatLogin] {}의 스탯을 불러오지 못해 기본값으로 대체", player.getName().getString());
        }

        // ✅ 스탯 동기화
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

        // ✅ 전투력 동기화
        double power = CombatPowerCalculator.calculate(player);
        AnsanPack.NETWORK.sendTo(
                new MessageSyncCombatPower(power),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );

        AnsanPack.LOGGER.info("[로그인] {} 전투력: {}", player.getName().getString(), power);

    }
}
