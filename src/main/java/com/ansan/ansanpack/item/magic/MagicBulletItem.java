package com.ansan.ansanpack.item.magic;

import com.ansan.ansanpack.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MagicBulletItem extends Item {

    public MagicBulletItem(Properties properties) {
        super(properties.stacksTo(64));
    }

    public static ItemStack createBullet(float magicPower) {
        ItemStack stack = new ItemStack(ModItems.MAGIC_BULLET_LOW.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat("MagicPower", 3.5f); // 공격력 저장
        return stack;
    }

    public static float getBulletPower(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("MagicPower")) {
            return stack.getTag().getFloat("MagicPower");
        }
        return 0f;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        float power = getBulletPower(stack); // 탄에 저장된 마법 공격력
        tooltip.add(Component.translatable("item.ansanpack.magic_bullet_low.tooltip", String.format("%.1f", power)).withStyle(ChatFormatting.GRAY));
    }
    @Override
    public @NotNull ItemStack getDefaultInstance() {
        return createBullet(3.5f); // 기본 마법 공격력 3.5로 생성
    }

}
