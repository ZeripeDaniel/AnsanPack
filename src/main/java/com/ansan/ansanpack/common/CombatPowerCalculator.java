package com.ansan.ansanpack.common;

import com.ansan.ansanpack.server.stat.ServerStatCache;
import com.ansan.ansanpack.server.stat.PlayerStat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import java.util.UUID;

public class CombatPowerCalculator {

    public static double calculate(ServerPlayer player) {
        double power = 0.0;

        // 1. 스탯 기반 전투력
        PlayerStat stat = ServerStatCache.get(player.getUUID());
        if (stat != null) {
            power += stat.getStrength() * 1.2;
            power += stat.getAgility() * 0.8;
            power += stat.getIntelligence() * 0.5;
        }

        // 2. 장비: 메인핸드 + 오프핸드 + 방어구
        power += extractPowerFromItem(player.getMainHandItem());
        power += extractPowerFromItem(player.getOffhandItem());
        for (ItemStack armor : player.getInventory().armor) {
            power += extractPowerFromItem(armor);
        }

        // 3. 버프 효과
        if (player.hasEffect(MobEffects.DAMAGE_BOOST)) {
            MobEffectInstance eff = player.getEffect(MobEffects.DAMAGE_BOOST);
            power += 5 + (eff != null ? eff.getAmplifier() * 3 : 0);
        }
        if (player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            power += 3;
        }
        if (player.hasEffect(MobEffects.REGENERATION)) {
            power += 2;
        }
        if (player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            power += 1.5;
        }
        if (player.hasEffect(MobEffects.HEALTH_BOOST)) {
            MobEffectInstance eff = player.getEffect(MobEffects.HEALTH_BOOST);
            power += (eff != null ? (eff.getAmplifier() + 1) * 2 : 2);
        }
        if (player.hasEffect(MobEffects.ABSORPTION)) {
            power += 2;
        }
        if (player.hasEffect(MobEffects.DIG_SPEED)) {
            power += 1;
        }
        if (player.hasEffect(MobEffects.JUMP)) {
            power += 1;
        }
        if (player.hasEffect(MobEffects.HEAL)) {
            power += 2;
        }


        return Math.round(power * 100.0) / 100.0;
    }

    private static double extractPowerFromItem(ItemStack item) {
        if (item == null || item.isEmpty()) return 0.0;

        double value = 0.0;

        // 1. 기본 능력치 (무기/방어구)
        if (item.getItem() instanceof TieredItem tiered) {
            value += tiered.getTier().getAttackDamageBonus() * 1.5;
        }
        if (item.getItem() instanceof ArmorItem armorItem) {
            value += armorItem.getDefense() * 1.0;
        }

        // 2. 강화된 NBT 수치
        if (item.hasTag()) {
            CompoundTag tag = item.getTag();

            value += tag.getDouble("extra_damage") * 2.0;
            value += tag.getDouble("extra_attack_speed") * 1.2;
            value += tag.getDouble("knockback_level") * 0.5;

            value += tag.getDouble("helmet_armor") * 0.8;
            value += tag.getDouble("chest_armor") * 0.8;
            value += tag.getDouble("leggings_armor") * 0.8;
            value += tag.getDouble("boots_armor") * 0.8;

            value += tag.getDouble("health_bonus") * 0.5;
            value += tag.getDouble("resist_knockback") * 0.4;
            value += tag.getDouble("toughness_bonus") * 0.6;
            value += tag.getDouble("move_speed_bonus") * 0.3;

            // luck_bonus 제외
        }

        return value;
    }
}
