package com.ansan.ansanpack.item;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.server.stat.PlayerStat;
import com.ansan.ansanpack.server.stat.ServerStatCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class CustomPotionItem extends Item {

    private final float healPercentage;
    private final int cooldownTicks;
    private final boolean removesNegativeEffects;

    public CustomPotionItem(Properties properties, float healPercentage, int cooldownTicks, boolean removesNegativeEffects) {
        super(properties);
        this.healPercentage = healPercentage;
        this.cooldownTicks = cooldownTicks;
        this.removesNegativeEffects = removesNegativeEffects;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!player.getCooldowns().isOnCooldown(this)) {
            if (!level.isClientSide) {
                float maxHealth = player.getMaxHealth();
                float healAmount = maxHealth * healPercentage;

                // 본인 회복
                player.heal(healAmount);
                player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 0));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0)); // 10초간 체력재생

                // 부정 효과 제거
                if (removesNegativeEffects) {
                    for (MobEffectInstance effect : player.getActiveEffects()) {
                        if (!effect.getEffect().isBeneficial()) {
                            player.removeEffect(effect.getEffect());
                        }
                    }
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 0.5F,
                        level.getRandom().nextFloat() * 0.1F + 0.9F);

                player.getCooldowns().addCooldown(this, cooldownTicks);
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                // 힐 공유
                if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
                    PlayerStat stat = ServerStatCache.get(serverPlayer.getUUID());
                    if (stat != null && stat.getIntelligence() > 0) {
                        int intel = stat.getIntelligence();
                        float shareRate = Math.min(0.1f + intel * 0.01f, 0.4f);
                        float sharedHeal = healAmount * shareRate;
                        int roundedHeal = Math.round(sharedHeal);

                        for (ServerPlayer other : serverLevel.getPlayers(p -> p != serverPlayer && p.distanceTo(serverPlayer) <= 15)) {
                            other.heal(sharedHeal);
                            other.sendSystemMessage(Component.literal("")
                                    .append(Component.literal(serverPlayer.getName().getString()).withStyle(ChatFormatting.WHITE))
                                    .append(Component.literal("님이 회복을 도와주었습니다! ").withStyle(ChatFormatting.GREEN))
                                    .append(Component.literal(String.valueOf(roundedHeal)).withStyle(ChatFormatting.YELLOW)));
                        }
                    }
                }
            }

            return InteractionResultHolder.consume(itemstack);
        }

        return InteractionResultHolder.fail(itemstack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }
}

