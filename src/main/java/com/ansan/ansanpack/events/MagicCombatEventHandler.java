package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.skills.ModAttributes;
import com.ansan.ansanpack.skills.SkillUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MagicCombatEventHandler {

    public static void applyMagicDamage(Entity sourceEntity, LivingEntity target, float baseMagicDamage) {
        if (target == null || sourceEntity == null || target == sourceEntity) return;

        double flatDef = SkillUtils.getAttributeValue(target, ModAttributes.MAGIC_DEFENSE_FLAT.get());
        double pctDef = SkillUtils.getAttributeValue(target, ModAttributes.MAGIC_DEFENSE_PERCENT.get());

        // 공격자 마법 공격력 적용
        float playerMagicAttackBonus = 0f;
        if (sourceEntity instanceof LivingEntity attacker) {
            playerMagicAttackBonus = (float) SkillUtils.getAttributeValue(attacker, ModAttributes.MAGIC_ATTACK.get());
        }

        float damageAfterFlat = Math.max(baseMagicDamage + playerMagicAttackBonus - (float) flatDef, 0f);
        float finalDamage = damageAfterFlat * (1.0f - (float) Math.min(pctDef, 1.0f));

        if (finalDamage <= 0f) return;

        DamageSource source;

        if (sourceEntity instanceof Player player) {
            source = target.damageSources().indirectMagic(player, player);
        } else if (sourceEntity instanceof Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                source = target.damageSources().indirectMagic(projectile, livingOwner);
            } else {
                source = target.damageSources().magic();
            }
        } else {
            source = target.damageSources().magic();
        }

        target.hurt(source, finalDamage);

//        AnsanPack.LOGGER.info("[MAGIC] {} → {} | base: {}, bonus: {}, flatDef: {}, pctDef: {}, final: {}",
//                sourceEntity.getName().getString(),
//                target.getName().getString(),
//                baseMagicDamage, playerMagicAttackBonus, flatDef, pctDef, finalDamage);
    }

//    public static void applyMagicDamage(Entity sourceEntity, LivingEntity target, float baseMagicDamage) {
//        if (target == null || sourceEntity == null || target == sourceEntity) return;
//
//        double flatDef = SkillUtils.getAttributeValue(target, ModAttributes.MAGIC_DEFENSE_FLAT.get());
//        double pctDef = SkillUtils.getAttributeValue(target, ModAttributes.MAGIC_DEFENSE_PERCENT.get());
//
//        float damageAfterFlat = Math.max(baseMagicDamage - (float) flatDef, 0f);
//        float finalDamage = damageAfterFlat * (1.0f - (float) Math.min(pctDef, 1.0f));
//
//        if (finalDamage <= 0f) return;
//
//        DamageSource source;
//
//        if (sourceEntity instanceof Player player) {
//            source = target.damageSources().indirectMagic(player, player);
//        } else if (sourceEntity instanceof Projectile projectile) {
//            Entity owner = projectile.getOwner();
//            if (owner instanceof LivingEntity livingOwner) {
//                source = target.damageSources().indirectMagic(projectile, livingOwner);
//            } else {
//                source = target.damageSources().magic();
//            }
//        } else {
//            source = target.damageSources().magic();
//        }
//
//        target.hurt(source, finalDamage);
//
//        AnsanPack.LOGGER.info("[MAGIC] {} dealt {:.2f} magic damage to {} (base: {:.2f}, flatDef: {:.2f}, pctDef: {:.2f})",
//                sourceEntity.getName().getString(), finalDamage,
//                target.getName().getString(), baseMagicDamage, flatDef, pctDef);
//    }
}
