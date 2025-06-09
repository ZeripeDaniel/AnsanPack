package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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

            // ✅ 효과 중첩 누적 적용
            for (var entry : config.effects.entrySet()) {
                String effect = entry.getKey();
                List<UpgradeConfigManager.EffectEntry> effectList = entry.getValue();

                for (var eff : effectList) {
                    if (level >= eff.applyLevel) {
                        int start = eff.applyLevel;
                        int end = level;
                        double total = 0;

                        for (int lv = start; lv <= end; lv++) {
                            int fakeLevel = lv - start + 1;  // 착각한 레벨
                            total += eff.value * WeaponUpgradeSystem.getEffectMultiplier(fakeLevel);
                        }



                        switch (effect) {
                            case "damage_per_level" -> bonusDamage += total;
                            case "knockback_level" -> knockbackBonus += total;
                        }
                    }
                }
            }

            // 🔁 데미지 보정 적용
            if (bonusDamage > 0) {
                event.setAmount(baseDamage + bonusDamage);
            }

            // 🔁 넉백 직접 적용
            if (knockbackBonus > 0) {
                double strength = knockbackBonus;
                double x = -Math.sin(Math.toRadians(player.getYRot()));
                double z = Math.cos(Math.toRadians(player.getYRot()));
                event.getEntity().knockback(strength, x, z);
            }
        });
    }

}
