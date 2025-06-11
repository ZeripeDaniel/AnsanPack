package com.ansan.ansanpack.client;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import com.ansan.ansanpack.network.MessageRequestCombatPowerRefresh;
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
    private static final int MONEY_SAVE_INTERVAL_TICKS = 20 * 10; // 5초

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != ClientTickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;

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
            AnsanPack.LOGGER.warn("[AutoSave] 어어 클라에서 보낸다 보내레벨스탯다보낸다 level:" + level + " exp : " + exp +
                    "str" + LocalPlayerStatData.INSTANCE.getStat("str") +
                    "agi" + LocalPlayerStatData.INSTANCE.getStat("agi") +
                    "int" + LocalPlayerStatData.INSTANCE.getStat("int") +
                    "luck" + LocalPlayerStatData.INSTANCE.getStat("luck") + "AP" + LocalPlayerStatData.INSTANCE.getAvailableAP()
            );
        }

        if (money_tickCounter >= MONEY_SAVE_INTERVAL_TICKS) {
            money_tickCounter = 0;
            AnsanPack.NETWORK.sendToServer(new MessageRequestMoneyOnly());
            int level = LocalPlayerLevelData.INSTANCE.getLevel();
            double exp = LocalPlayerLevelData.INSTANCE.getExp();
            double maxExp = LocalPlayerLevelData.INSTANCE.getExpToNextLevel();

            int str = LocalPlayerStatData.INSTANCE.getStat("str");
            int agi = LocalPlayerStatData.INSTANCE.getStat("agi");
            int intel = LocalPlayerStatData.INSTANCE.getStat("int");
            int luck = LocalPlayerStatData.INSTANCE.getStat("luck");
            int ap = LocalPlayerStatData.INSTANCE.getAvailableAP();

            AnsanPack.LOGGER.info("[머니][레벨 상태] LV.{} | EXP: {:.3f} / {:.3f}", level, exp, maxExp);
            AnsanPack.LOGGER.info("[머니][스탯 상태] 힘: {}, 민첩: {}, 지능: {}, 행운: {}, AP: {}", str, agi, intel, luck, ap);
            // ✅ 전투력 재계산 요청 추가
            AnsanPack.NETWORK.sendToServer(new MessageRequestCombatPowerRefresh());

            AnsanPack.LOGGER.warn("[AutoSave] 전투력 재계산 요청 전송함");
        }

    }
}
