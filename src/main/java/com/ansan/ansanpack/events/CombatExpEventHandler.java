package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.network.MessageGainExp;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = "ansanpack", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatExpEventHandler {

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity target = event.getEntity(); // ✅ 불필요한 instanceof 제거

        int gain = calculateExp(target);
        AnsanPack.NETWORK.sendTo(new MessageGainExp(gain), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static int calculateExp(LivingEntity target) {
        double maxHealth = target.getMaxHealth();
        return (int) Math.max(1, Math.min(10, maxHealth / 5.0)); // 예: 체력이 높을수록 더 많은 EXP
    }
}
