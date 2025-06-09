package com.ansan.ansanpack.skills;

import com.ansan.ansanpack.AnsanPack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID)
public class SkillEffectEventHandler {

    // ✅ Block 파괴 이벤트
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        FarmerSkillHandler.handleBlockBreak(event);
        MinerSkillHandler.handleBlockBreak(event);
    }

    // ✅ 파괴 속도 이벤트
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        MinerSkillHandler.handleBreakSpeed(event);
    }

    // ✅ 낚시 이벤트
    @SubscribeEvent
    public static void onFishing(ItemFishedEvent event) {
        FisherSkillHandler.handleFishing(event);
    }
}
