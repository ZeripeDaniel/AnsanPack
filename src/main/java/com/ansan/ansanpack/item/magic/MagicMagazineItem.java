package com.ansan.ansanpack.item.magic;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import com.ansan.ansanpack.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class MagicMagazineItem extends Item {

    private static final int MAX_AMMO = 128;

    public MagicMagazineItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack magazine = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ItemStack bulletStack = findBulletFromInventory(player);
            //AnsanPack.LOGGER.info("[DEBUG] bulletStack.isEmpty(): " + bulletStack.isEmpty());

            if (!bulletStack.isEmpty() && getCurrentAmmo(magazine) < MAX_AMMO) {
                int bulletCount = bulletStack.getCount();

                // 16개보다 적으면 리턴
                if (bulletCount < 16) {
                    //AnsanPack.LOGGER.info("[DEBUG] Not enough bullets to load (need 16, have " + bulletCount + ")");
                    player.displayClientMessage(Component.literal("§c탄약이 부족합니다. (16개 필요)"), true);
                    return InteractionResultHolder.success(magazine);
                }

                float bulletPower = MagicBulletItem.getBulletPower(bulletStack);
                Item bulletItem = bulletStack.getItem();

                //AnsanPack.LOGGER.info("[DEBUG] bulletPower: " + bulletPower);
                //AnsanPack.LOGGER.info("[DEBUG] bulletItem: " + bulletItem);
                //AnsanPack.LOGGER.info("[DEBUG] canLoadBullet: " + canLoadBullet(magazine, bulletItem));

                if (canLoadBullet(magazine, bulletItem)) {
                    int ammo = getCurrentAmmo(magazine);
                    setCurrentAmmo(magazine, ammo + 16);
                    setBulletPower(magazine, bulletPower);
                    setBulletType(magazine, bulletItem);

                    //AnsanPack.LOGGER.info("[DEBUG] AMMO SET: " + (ammo + 16));

                    bulletStack.shrink(16);
                    player.getCooldowns().addCooldown(this, 10);
                }
            }
        }
        return InteractionResultHolder.success(magazine);
    }


    private ItemStack findBulletFromInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == ModItems.MAGIC_BULLET_LOW.get()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean canLoadBullet(ItemStack magazine, Item bulletItem) {
        Item loadedType = getBulletType(magazine);
        if (loadedType == Items.AIR) {
            loadedType = null;
        }
        return loadedType == null || loadedType == bulletItem;
    }

    // --- NBT 관리 ---
    public static int getCurrentAmmo(ItemStack stack) {
        return stack.getOrCreateTag().getInt("CurrentAmmo");
    }

    public static void setCurrentAmmo(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt("CurrentAmmo", Math.min(value, MAX_AMMO));
    }

    public static void setBulletPower(ItemStack stack, float power) {
        stack.getOrCreateTag().putFloat("AmmoPower", power);
    }

    public static float getBulletPower(ItemStack stack) {
        return stack.getOrCreateTag().getFloat("AmmoPower");
    }
    @SuppressWarnings("deprecation")
    public static void setBulletType(ItemStack stack, Item bulletItem) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(bulletItem);
        if (key != null) {
            stack.getOrCreateTag().putString("BulletItem", key.toString());
        }
    }
    @SuppressWarnings("deprecation")
    public static Item getBulletType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("BulletItem")) {
            return BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("BulletItem")));
        }
        return Items.AIR;
    }

    public static float getTotalMagicPower(ItemStack stack) {
        return getBulletPower(stack); // 지금은 한 종류만 장전 가능하므로 그대로 반환
    }

    // 탄 소비
    public static boolean consumeBullet(ItemStack stack, int amount) {
        int current = getCurrentAmmo(stack);
        if (current >= amount) {
            setCurrentAmmo(stack, current - amount);
            return true;
        }
        return false;
    }

    // 현재 마법 공격력 반환 (탄창의 총 위력)
    public static float getMagicPower(ItemStack stack) {
        return getTotalMagicPower(stack); // 혹은 향후 종류별로 확장 시 계산식 추가 가능
    }

    // 장전된 탄창 찾기
    public static ItemStack findLoadedMagazine(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof MagicMagazineItem && getCurrentAmmo(stack) > 0) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        int ammo = getCurrentAmmo(stack);
        float power = getBulletPower(stack);
        Item bulletType = getBulletType(stack);

        String bulletName = bulletType != null
                ? bulletType.getDescription().getString()
                : "없음";

        tooltip.add(Component.translatable("item.ansanpack.magic_magazine.tooltip", ammo, bulletName, power));
    }
    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        setCurrentAmmo(stack, 128);
        setBulletPower(stack, 3.5f);
        setBulletType(stack, ModItems.MAGIC_BULLET_LOW.get());
        return stack;
    }
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCurrentAmmo(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int ammo = getCurrentAmmo(stack);
        return Math.round(13.0f * ammo / MAX_AMMO);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, (float)getCurrentAmmo(stack) / (float)MAX_AMMO);

        // 빨강 → 노랑 → 초록 전환
        int i = Math.round(255.0F * (1.0F - f)); // Red 감소
        int j = Math.round(255.0F * f);          // Green 증가

        return (i << 16) | (j << 8); // RGB: (R,G,0)
    }


}
