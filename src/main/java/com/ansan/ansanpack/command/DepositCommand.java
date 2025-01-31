package com.ansan.ansanpack.command;

import com.ansan.ansanpack.item.ModItems;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class DepositCommand {

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = (ServerPlayer) source.getEntity();

        if (player == null) {
            source.sendFailure(Component.literal("이 명령어는 플레이어만 사용할 수 있습니다!"));
            return Command.SINGLE_SUCCESS;
        }

        // 스코어보드 가져오기
        Scoreboard scoreboard = player.getScoreboard();
        String scoreName = "ansan_money";
        if (scoreboard.getObjective(scoreName) == null) {
            scoreboard.addObjective(scoreName, ObjectiveCriteria.DUMMY, Component.literal("안산 머니"), ObjectiveCriteria.RenderType.INTEGER);
        }

        // 인벤토리 확인 및 코인 제거
        int totalScore = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.ONE_COIN.get())) {
                totalScore += stack.getCount();
                stack.setCount(0); // 제거
            } else if (stack.is(ModItems.TEN_COIN.get())) {
                totalScore += stack.getCount() * 10;
                stack.setCount(0); // 제거
            } else if (stack.is(ModItems.HUNDRED_COIN.get())) {
                totalScore += stack.getCount() * 100;
                stack.setCount(0); // 제거
            } else if (stack.is(ModItems.THOUSAND_COIN.get())) {
                totalScore += stack.getCount() * 1000;
                stack.setCount(0); // 제거
            }
        }

        // 스코어보드에 등록
        Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), scoreboard.getObjective(scoreName));
        score.setScore(score.getScore() + totalScore);

        // 완료 메시지
        if (totalScore > 0) {
            final int finalTotalScore = totalScore;
            source.sendSuccess(() -> Component.literal("총 " + finalTotalScore + " 안공을 등록했습니다!").withStyle(ChatFormatting.GREEN), true);

        } else {
            source.sendFailure(Component.literal("등록할 안공이 없습니다!"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("입금")
                .requires(source -> source.getEntity() instanceof ServerPlayer) // 플레이어만 사용 가능
                .executes(DepositCommand::execute));
    }
}
