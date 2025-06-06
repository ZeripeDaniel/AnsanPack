package com.ansan.ansanpack.client;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import com.ansan.ansanpack.network.MessageRequestSaveLevel;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ansanpack", value = Dist.CLIENT)
public class LevelTickHandler {

    private static int tickCount = 0;
    private static final int TICK_INTERVAL = 20 * 60 * 10; // 10분 (20tps 기준)

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != ClientTickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        tickCount++;

        if (tickCount >= TICK_INTERVAL) {
            tickCount = 0;

            int level = LocalPlayerLevelData.INSTANCE.getLevel();
            int exp = LocalPlayerLevelData.INSTANCE.getExp();
            AnsanPack.NETWORK.sendToServer(new MessageRequestSaveLevel(level, exp));
        }
    }
}
