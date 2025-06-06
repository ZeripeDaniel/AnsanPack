package com.ansan.ansanpack.item;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.server.stat.PlayerStat;
import com.ansan.ansanpack.server.stat.ServerStatCache;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class CustomPotionItem extends Item {

    private final float healPercentage;
    private static final int USE_DURATION = 20; // 1초
    private static final int COOLDOWN = 200;    // 10초

    public CustomPotionItem(Properties properties, float healPercentage) {
        super(properties);
        this.healPercentage = healPercentage;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!player.getCooldowns().isOnCooldown(this)) {
            player.startUsingItem(hand);
            AnsanPack.LOGGER.info("[힐] 플레이어 {} 가 힐 아이템 사용 시작", player.getName().getString());


            if (!level.isClientSide) {
                level.getServer().tell(new TickTask(USE_DURATION, () -> {
                    float maxHealth = player.getMaxHealth();
                    float healAmount = maxHealth * healPercentage;

                    // 본인 회복
                    player.heal(healAmount);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 0.5F,
                            level.getRandom().nextFloat() * 0.1F + 0.9F);

                    player.getCooldowns().addCooldown(this, COOLDOWN);
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    AnsanPack.LOGGER.info("[힐] 본인 회복량: {}", healAmount);


                    // 힐 공유
                    if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
                        PlayerStat stat = ServerStatCache.get(serverPlayer.getUUID());
                        if (stat == null) {
                            AnsanPack.LOGGER.warn("[힐] ServerStatCache에서 스탯을 찾을 수 없습니다: {}", player.getUUID());
                        }
                        assert stat != null;
                        int intel = stat.getIntelligence();

                        float shareRate = 0.1f + intel * 0.01f;
                        if (shareRate > 0.4f) shareRate = 0.4f;

                        float sharedHeal = healAmount * shareRate;
                        int roundedHeal = Math.round(sharedHeal);

                        AnsanPack.LOGGER.info("[힐] 지능 {} → 공유 힐 비율: {} / 실제 공유량: {}", intel, shareRate, sharedHeal);
                        for (ServerPlayer other : serverLevel.getPlayers(p ->
                                p != serverPlayer && p.distanceTo(serverPlayer) <= 15)) {
                            other.heal(sharedHeal);

                            // 메시지 출력
                            other.sendSystemMessage(
                                    Component.literal("")
                                            .append(Component.literal(serverPlayer.getName().getString()).withStyle(ChatFormatting.WHITE))
                                            .append(Component.literal("님이 회복을 도와주었습니다! ").withStyle(ChatFormatting.GREEN))
                                            .append(Component.literal(String.valueOf(roundedHeal)).withStyle(ChatFormatting.YELLOW))
                            );
                            AnsanPack.LOGGER.info("[힐] {}님이 {}님에게 {} 회복 공유", serverPlayer.getName().getString(), other.getName().getString(), roundedHeal);
                        }
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
