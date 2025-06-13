package com.ansan.ansanpack.item.magic;

import com.ansan.ansanpack.item.ModItems;
import com.ansan.ansanpack.skills.ModAttributes;
import com.ansan.ansanpack.skills.SkillUtils;
import com.ansan.ansanpack.util.ClientHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.UUID;

public class ManaBlasterItem extends Item {

    private final int fireRateTicks = 6;
    private final int fireCount = 4;
    private final float projectileSpeed = 1.6f;
    private int cooldownTicks = 0;

    private boolean hasFired = false;

    public ManaBlasterItem(Properties props) {
        super(props);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack magazine = MagicMagazineItem.findLoadedMagazine(player);

        if (magazine.isEmpty() || MagicMagazineItem.getCurrentAmmo(magazine) <= 0) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (!(entity instanceof Player player)) return;

        // 쿨타임 처리
        if (cooldownTicks > 0) {
            cooldownTicks--;
            if (cooldownTicks <= 0) {
                hasFired = false; // 쿨타임 끝 → 다시 발사 가능
            }
            return;
        }

        int charge = this.getUseDuration(stack) - count;

        // charge >= 25 틱 이상 유지중이고, 쿨타임 없음, 아직 발사 안한 경우
        if (charge >= 25 && !level.isClientSide && count % fireRateTicks == 0 && !hasFired) {
            ItemStack magazine = MagicMagazineItem.findLoadedMagazine(player);
            if (magazine.isEmpty() || MagicMagazineItem.getCurrentAmmo(magazine) <= 0) return;

            int currentAmmo = MagicMagazineItem.getCurrentAmmo(magazine);
            Item bulletType = MagicMagazineItem.getBulletType(magazine);
            float bulletPower = MagicMagazineItem.getBulletPower(magazine);

            if (bulletType == null || currentAmmo <= 0 || bulletPower <= 0f) return;

            int cost = (int) Math.ceil(128.0 / fireCount);
            if (currentAmmo < cost) return;

            float playerMagicAttackBonus = (float) SkillUtils.getAttributeValue(player, ModAttributes.MAGIC_ATTACK.get());
            float totalDamage = bulletPower + playerMagicAttackBonus;

            MagicProjectileEntity proj = new MagicProjectileEntity(level, player, totalDamage);
            proj.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, projectileSpeed, 0.0F);
            level.addFreshEntity(proj);

            MagicMagazineItem.consumeBullet(magazine, cost);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.7F, 1.0F);

            player.awardStat(Stats.ITEM_USED.get(this));

            // 쿨타임 설정 및 발사 flag 설정
            cooldownTicks = 40; // 예: 40틱 = 2초
            hasFired = true;
        }
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ansanpack.mana_blaster.tooltip", fireCount).withStyle(ChatFormatting.GRAY));

        if (world != null && world.isClientSide) {
            double playerMagicAttack = getClientPlayerMagicAttackSafe();
            tooltip.add(Component.literal("§7Magic Attack: +" + String.format("%.1f", playerMagicAttack)));
        }

        tooltip.add(Component.literal("§7Attack Speed: " + String.format("%.2f", getAttackSpeed()) + " shots/sec"));
    }

    private double getAttackSpeed() {
        return 20.0 / fireRateTicks;
    }

    private double getClientPlayerMagicAttackSafe() {
        // 안전한 클라이언트 전용 호출
        return ClientHelper.getClientPlayerMagicAttack();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

        if (slot == EquipmentSlot.MAINHAND) {
            builder.put(Attributes.ATTACK_SPEED,
                    new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.0F, AttributeModifier.Operation.ADDITION));

            builder.put(ModAttributes.MAGIC_ATTACK.get(),
                    new AttributeModifier(UUID.randomUUID(), "Magic attack bonus", 0.0F, AttributeModifier.Operation.ADDITION));
        }

        return builder.build();
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        super.releaseUsing(stack, level, entityLiving, timeLeft);
        hasFired = false; // 활 사용 종료시 다시 초기화
        cooldownTicks = 0;
    }
}
