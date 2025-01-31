package com.ansan.ansanpack.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class BalanceCommand {

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
        Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), scoreboard.getObjective(scoreName));
        int balance = score.getScore();

        // 메시지 출력
        source.sendSuccess(() -> Component.literal("현재 잔액: " + balance + " 안공").withStyle(ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("잔액")
                .requires(source -> source.getEntity() instanceof ServerPlayer) // 플레이어만 사용 가능
                .executes(BalanceCommand::execute));
    }
}
