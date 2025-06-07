package com.ansan.ansanpack.client;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import com.ansan.ansanpack.network.MessageRequestMoneyOnly;
import com.ansan.ansanpack.network.MessageRequestSaveLevel;

import com.ansan.ansanpack.network.MessageRequestSaveStats;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, value = Dist.CLIENT)
public class LevelTickHandler {

    private static int tickCounter = 0;
    private static int money_tickCounter = 0;
    private static final int SAVE_INTERVAL_TICKS = 20 * 60 * 3; // 3분
    private static final int MONEY_SAVE_INTERVAL_TICKS = 20 * 5; // 5초

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != ClientTickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;

        tickCounter++;
        money_tickCounter++;

        if (tickCounter >= SAVE_INTERVAL_TICKS) {
            tickCounter = 0;

            int level = LocalPlayerLevelData.INSTANCE.getLevel();
            double exp = LocalPlayerLevelData.INSTANCE.getExp();

            AnsanPack.NETWORK.sendToServer(new MessageRequestSaveLevel(level, exp));
            AnsanPack.NETWORK.sendToServer(new MessageRequestSaveStats(
                    LocalPlayerStatData.INSTANCE.getStat("str"),
                    LocalPlayerStatData.INSTANCE.getStat("agi"),
                    LocalPlayerStatData.INSTANCE.getStat("int"),
                    LocalPlayerStatData.INSTANCE.getStat("luck"),
                    LocalPlayerStatData.INSTANCE.getAvailableAP()
            ));

            //AnsanPack.LOGGER.warn("[AutoSave] level={}, exp={}", level, exp);
            AnsanPack.LOGGER.warn("[AutoSave] 어어 클라에서 보낸다 보내레벨스탯다보낸다 level:" + level + " exp : " + exp);
        }

        // 5초마다 요청하고 싶다면:
        if (money_tickCounter >= MONEY_SAVE_INTERVAL_TICKS) {
            money_tickCounter = 0;
            AnsanPack.NETWORK.sendToServer(new MessageRequestMoneyOnly());
        }

    }
}
