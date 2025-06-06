package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;

        UpgradeConfigManager.getConfig(weapon.getItem()).ifPresent(config -> {
            int level = WeaponUpgradeSystem.getCurrentLevel(weapon);
            if (level <= 0) return;

            float baseDamage = event.getAmount();
            float bonusDamage = 0f;
            float knockbackBonus = 0f;

            // 여러 효과 계산
            for (var entry : config.effects.entrySet()) {
                String effect = entry.getKey();
                double value = entry.getValue();
                double scaled = value * level;

                switch (effect) {
                    case "damage_per_level" -> bonusDamage += scaled;
                    case "knockback_level" -> knockbackBonus += scaled;
                    // 나중에 다른 효과 필요하면 여기에 추가
                }
            }

            // 데미지 보정 적용
            if (bonusDamage > 0) {
                event.setAmount(baseDamage + bonusDamage);
            }

            // 넉백 직접 적용 (LivingHurtEvent 시점에는 applyKnockback 가능)
            if (knockbackBonus > 0) {
                double strength = knockbackBonus;
                double x = -Math.sin(Math.toRadians(player.getYRot()));
                double z = Math.cos(Math.toRadians(player.getYRot()));
                event.getEntity().knockback(strength, x, z);
            }

            // DEBUG 로그
//            AnsanPack.LOGGER.info("[전투] {} → {} 데미지: {} + {}, 넉백: {}",
//                    player.getName().getString(),
//                    event.getEntity().getName().getString(),
//                    baseDamage, bonusDamage, knockbackBonus);
        });
    }
}
