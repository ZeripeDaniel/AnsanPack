package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.network.MessageGainExp;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatExpEventHandler {

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity target = event.getEntity(); // ✅ 불필요한 instanceof 제거

        double gain = calculateExp(target);
        AnsanPack.NETWORK.sendTo(new MessageGainExp(gain), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);

        //double attack = player.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
        //double speed = player.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
       // double luck = player.getAttribute(Attributes.LUCK).getValue();

        //AnsanPack.LOGGER.info("[전투 로그] {} 공격력: {}, 공속: {}, 행운: {}",
                //player.getName().getString(), attack, speed, luck);
    }

    private static double calculateExp(LivingEntity target) {
        double maxHealth = target.getMaxHealth();

        // 기본 EXP = log2(체력 + 1) * 2.5
        // → 체력 10 → 약 8.3
        // → 체력 100 → 약 16.6
        // → 체력 200 → 약 19.1
        double baseExp = Math.log(maxHealth + 1) / Math.log(2) * 2.5;

        // 난이도 가중치 조정 (체력 구간별 페널티/보너스)
        if (maxHealth <= 10) {
            baseExp *= 1.0; // 약한 몹은 적게
        } else if (maxHealth >= 100 && maxHealth < 200) {
            baseExp *= 1.6; // 중간몹 보정
        } else if (maxHealth >= 200 && maxHealth < 400) {
            baseExp *= 2.0; // 보스급 보정
        } else if (maxHealth >= 400) {
            baseExp *= 3.0; // 보스급 보정
        }

        return Math.round(baseExp * 100.0) / 100.0; // 소수점 2자리 반올림
    }
}
