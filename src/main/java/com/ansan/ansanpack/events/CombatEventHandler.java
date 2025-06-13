package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.skills.ModAttributes;
import com.ansan.ansanpack.skills.SkillUtils;
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

        float baseDamage = event.getAmount();
        float[] bonusDamage = {0f};
        float[] knockbackBonus = {0f};

        //무기 강화 효과 계산
        UpgradeConfigManager.getConfig(weapon.getItem()).ifPresent(config -> {
            int level = WeaponUpgradeSystem.getCurrentLevel(weapon);
            if (level <= 0) return;

            for (var entry : config.effects.entrySet()) {
                String effect = entry.getKey();
                List<UpgradeConfigManager.EffectEntry> effectList = entry.getValue();

                for (var eff : effectList) {
                    if (level >= eff.applyLevel) {
                        int start = eff.applyLevel;
                        int end = level;
                        double total = 0;

                        for (int lv = start; lv <= end; lv++) {
                            int fakeLevel = lv - start + 1;
                            total += eff.value * WeaponUpgradeSystem.getEffectMultiplier(fakeLevel);
                        }

                        switch (effect) {
                            case "damage_per_level" -> bonusDamage[0] += total;
                            case "knockback_level" -> knockbackBonus[0] += total;
                        }
                    }
                }
            }
        });

        //데미지 보정 적용
        if (bonusDamage[0] > 0) {
            event.setAmount(baseDamage + bonusDamage[0]);
        }

        //넉백 보정 적용
        if (knockbackBonus[0] > 0) {
            double strength = knockbackBonus[0];
            double x = -Math.sin(Math.toRadians(player.getYRot()));
            double z = Math.cos(Math.toRadians(player.getYRot()));
            event.getEntity().knockback(strength, x, z);
        }

        // 크리티컬 데미지 먼저 적용
        double critChance = SkillUtils.getAttributeValue(player, ModAttributes.CRITICAL_CHANCE.get());
        double critDamage = SkillUtils.getAttributeValue(player, ModAttributes.CRITICAL_DAMAGE.get());

        critChance = Math.min(critChance, 100.0);
        critDamage = Math.min(critDamage, 3.0);

        if (player.level().random.nextDouble() * 100 < critChance) {
            float current = event.getAmount();
            float crit = (float)(current * (critDamage - 1.0));
            event.setAmount(current + crit);
            AnsanPack.LOGGER.info("[CRITICAL] {} dealt CRIT! {} → {} (×{})", player.getName().getString(), current, current + crit, critDamage);
        }

        // 크리티컬까지 반영된 데미지 기준으로 흡혈 처리
        double chance = SkillUtils.getAttributeValue(player, ModAttributes.LIFESTEAL_CHANCE.get());
        double ratio = SkillUtils.getAttributeValue(player, ModAttributes.LIFESTEAL_RATIO.get());
        double maxHeal = SkillUtils.getAttributeValue(player, ModAttributes.LIFESTEAL_MAX_AMOUNT.get());

        if (player.level().random.nextFloat() < chance) {
            float damage = event.getAmount(); // 최종 데미지 기준
            float healAmount = Math.min((float)(damage * ratio), (float)maxHeal);
            if (healAmount > 0) {
                player.heal(healAmount);
                AnsanPack.LOGGER.info("[LIFESTEAL] {} healed {} ({} damage × {} ratio)", player.getName().getString(), healAmount, damage, ratio);
            }
        }
    }

}
