package com.ansan.ansanpack.server;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.common.CombatPowerCalculator;
import com.ansan.ansanpack.config.CombatPowerDatabaseManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatPowerSaveScheduler {

    private static int tickCounter = 0;
    private static final int SAVE_INTERVAL_TICKS = 20 * 60 * 3; // 3분

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;

        if (tickCounter >= SAVE_INTERVAL_TICKS) {
            tickCounter = 0;

            MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                double power = CombatPowerCalculator.calculate(player);
                CombatPowerDatabaseManager.saveCombatPower(player.getUUID(), player.getName().getString(), power);
                AnsanPack.LOGGER.info("[전투력 자동저장] {} → {}", player.getName().getString(), power);
            }
        }
    }
}
