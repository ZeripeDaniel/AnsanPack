//package com.ansan.ansanpack.client.level;
//
//import com.ansan.ansanpack.AnsanPack;
//import com.ansan.ansanpack.network.MessageRequestMoneyOnly;
//import com.ansan.ansanpack.network.MessageRequestSaveLevel;
//import com.ansan.ansanpack.network.MessageRequestSaveStats;
//import net.minecraft.client.Minecraft;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
///**
// * 클라이언트 틱마다 자동 저장 트리거 (10분 주기)
// */
//@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
//public class LevelTickHandler {
//
//    private static int tickCounter = 0;
//    private static final int SAVE_INTERVAL_TICKS = 20 * 60 * 3; // 3분 (3600틱)
//
//    @SubscribeEvent
//    public static void onClientTick(TickEvent.ClientTickEvent event) {
//        if (event.phase != TickEvent.Phase.END) return;
//        if (Minecraft.getInstance().player == null) return;
//
//        tickCounter++;
//
//        if (tickCounter >= SAVE_INTERVAL_TICKS) {
//            tickCounter = 0;
//
//            int level = LocalPlayerLevelData.INSTANCE.getLevel();
//            int exp = LocalPlayerLevelData.INSTANCE.getExp();
//
//            // 경험치 저장
//            AnsanPack.NETWORK.sendToServer(new MessageRequestSaveLevel(level, exp));
//
//            // 스탯 저장
//            AnsanPack.NETWORK.sendToServer(new MessageRequestSaveStats(
//                    LocalPlayerStatData.INSTANCE.getStat("str"),
//                    LocalPlayerStatData.INSTANCE.getStat("agi"),
//                    LocalPlayerStatData.INSTANCE.getStat("int"),
//                    LocalPlayerStatData.INSTANCE.getStat("luck"),
//                    LocalPlayerStatData.INSTANCE.getAvailableAP()
//            ));
//            AnsanPack.LOGGER.warn("어어 클라에서 보낸다 보내레벨스탯다보낸다 level:" + level + " exp : " + exp);
//        }
//        if (tickCounter % (20 * 60) == 0) {
//            AnsanPack.NETWORK.sendToServer(new MessageRequestMoneyOnly());
//        }
//
//    }
//}
