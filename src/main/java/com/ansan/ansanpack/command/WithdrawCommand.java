package com.ansan.ansanpack.command;

import com.ansan.ansanpack.item.ModItems;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class WithdrawCommand {

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = (ServerPlayer) source.getEntity();

        if (player == null) {
            source.sendFailure(Component.literal("이 명령어는 플레이어만 사용할 수 있습니다!"));
            return Command.SINGLE_SUCCESS;
        }

        int amount = IntegerArgumentType.getInteger(context, "amount");
        if (amount <= 0) {
            source.sendFailure(Component.literal("출금 금액은 0보다 커야 합니다!"));
            return Command.SINGLE_SUCCESS;
        }

        Scoreboard scoreboard = player.getScoreboard();
        String scoreName = "ansan_money";
        Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), scoreboard.getObjective(scoreName));
        int currentScore = score.getScore();

        if (currentScore < amount) {
            source.sendFailure(Component.literal("출금 금액이 스코어보드에 등록된 금액보다 많습니다!"));
            return Command.SINGLE_SUCCESS;
        }

        int requiredSlots = calculateRequiredSlots(player, amount);
        int freeSlots = countFreeInventorySlots(player);

        if (freeSlots < requiredSlots) {
            source.sendFailure(Component.literal("인벤토리에 공간이 부족합니다! 필요한 공간: " + requiredSlots + ", 사용 가능한 공간: " + freeSlots));
            return Command.SINGLE_SUCCESS;
        }

        giveCoins(player, ModItems.THOUSAND_COIN.get(), amount / 1000);
        giveCoins(player, ModItems.HUNDRED_COIN.get(), (amount % 1000) / 100);
        giveCoins(player, ModItems.TEN_COIN.get(), (amount % 100) / 10);
        giveCoins(player, ModItems.ONE_COIN.get(), amount % 10);

        score.setScore(currentScore - amount);

        source.sendSuccess(() -> Component.literal("총 " + amount + " 안공을 출금했습니다!").append(Component.literal(" 잔액: "+ score.getScore() + " 안공").withStyle(ChatFormatting.GREEN)), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int calculateRequiredSlots(ServerPlayer player, int amount) {
        int slots = 0;
        slots += calculateSlotsForCoin(player, ModItems.THOUSAND_COIN.get(), amount / 1000);
        slots += calculateSlotsForCoin(player, ModItems.HUNDRED_COIN.get(), (amount % 1000) / 100);
        slots += calculateSlotsForCoin(player, ModItems.TEN_COIN.get(), (amount % 100) / 10);
        slots += calculateSlotsForCoin(player, ModItems.ONE_COIN.get(), amount % 10);
        return slots;
    }

    private static int calculateSlotsForCoin(ServerPlayer player, Item coinItem, int count) {
        if (count == 0) return 0;
        int existingCount = countExistingItems(player, coinItem);
        int totalCount = existingCount + count;
        return (int) Math.ceil(totalCount / 64.0) - (int) Math.ceil(existingCount / 64.0);
    }

    private static int countExistingItems(ServerPlayer player, Item item) {
        return player.getInventory().items.stream()
                .filter(stack -> stack.getItem() == item)
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static int countFreeInventorySlots(ServerPlayer player) {
        return (int) player.getInventory().items.stream().filter(ItemStack::isEmpty).count();
    }

    private static void giveCoins(ServerPlayer player, Item coinItem, int count) {
        while (count > 0) {
            int stackSize = Math.min(count, coinItem.getMaxStackSize());
            ItemStack stack = new ItemStack(coinItem, stackSize);
            player.getInventory().add(stack);
            count -= stackSize;
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("출금")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(WithdrawCommand::execute)));
    }
}
