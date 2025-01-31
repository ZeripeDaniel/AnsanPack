package com.ansan.ansanpack.item;

import com.ansan.ansanpack.config.RandomBoxConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomItemBox extends Item {
    public RandomItemBox(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 플레이어의 인벤토리에 빈 슬롯이 있는지 확인
            if (player.getInventory().getFreeSlot() == -1) {
                player.sendSystemMessage(Component.literal("인벤토리에 빈 공간이 없습니다. 최소 한 칸의 여유 공간이 필요합니다.").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemstack);
            }

            giveRandomItems(player);
            itemstack.shrink(1); // 아이템 사용 후 제거
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    private void giveRandomItems(Player player) {
        List<RandomBoxConfigManager.RandomBoxItem> items = RandomBoxConfigManager.getItems();
        if (items.isEmpty()) return;

        Random random = new Random();
        double totalProbability = items.stream().mapToDouble(item -> item.probability).sum();
        double randomValue = random.nextDouble();

        if (randomValue < totalProbability) {
            // 기존 확률 로직
            double cumulativeProbability = 0.0;
            for (RandomBoxConfigManager.RandomBoxItem item : items) {
                cumulativeProbability += item.probability;
                if (randomValue < cumulativeProbability) {
                    giveItemToPlayer(player, item);
                    return;
                }
            }
        } else {
            // 확률에 들어가지 않았을 때 꽝 상품 지급
            giveConsolationPrize(player);
        }
    }

    private void giveConsolationPrize(Player player) {
        ResourceLocation consolationItemId = new ResourceLocation("ansanpack", "thousand_coin");
        Item consolationItem = ForgeRegistries.ITEMS.getValue(consolationItemId);

        if (consolationItem != null && consolationItem != Items.AIR) {
            ItemStack consolationPrize = new ItemStack(consolationItem, 3);
            player.addItem(consolationPrize);
            MutableComponent message = Component.literal("저런 꽝이네요 소정의 위로금입니다. ")
                    .append(Component.literal("3000안공").withStyle(ChatFormatting.YELLOW));

            player.sendSystemMessage(message);

//            player.sendSystemMessage(Component.literal("저런 꽝이네요 소정의 위로금입니다. 3000안공"));
        } else {
            player.sendSystemMessage(Component.literal("오류: 족버그입니다"));
        }
    }

    private void giveItemToPlayer(Player player, RandomBoxConfigManager.RandomBoxItem item) {
        Item actualItem = item.getItem();
        if (actualItem != Items.AIR) {
            ItemStack selectedItem = new ItemStack(actualItem, item.count);
            player.addItem(selectedItem);
            String displayName = item.giftname != null && !item.giftname.isEmpty() ? item.giftname : actualItem.getDescriptionId();
            MutableComponent message2 = Component.literal("받은 아이템: ").withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(String.valueOf(item.count))).withStyle(ChatFormatting.RED)
                    .append(Component.literal("x "))
                    .append(Component.literal(displayName).withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(message2);
        } else {
            player.sendSystemMessage(Component.literal("오류: 아이템을 찾을 수 없습니다 - " + item.itemName));
        }
    }
}
