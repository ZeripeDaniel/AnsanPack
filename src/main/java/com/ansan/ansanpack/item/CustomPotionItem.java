package com.ansan.ansanpack.item;

import net.minecraft.server.TickTask;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.UseAnim;

public class CustomPotionItem extends Item {
    private final float healPercentage;
    private static final int USE_DURATION = 20; // 1초 (32 ticks)
    private static final int COOLDOWN = 200; // 10초 (200 ticks)
    public CustomPotionItem(Properties properties, float healPercentage) {
        super(properties);
        this.healPercentage = healPercentage;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!player.getCooldowns().isOnCooldown(this)) {
            player.startUsingItem(hand);
            if (!level.isClientSide) {
                level.getServer().tell(new TickTask(USE_DURATION, () -> {
                    float maxHealth = player.getMaxHealth();
                    float healAmount = maxHealth * healPercentage;
                    player.heal(healAmount);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                    player.getCooldowns().addCooldown(this, COOLDOWN);

                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                }));
            }
            return InteractionResultHolder.consume(itemstack);
        }
        return InteractionResultHolder.fail(itemstack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }
}
