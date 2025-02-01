package com.ansan.ansanpack.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class TransferCommand {

    // SuggestionProvider to list online players for auto-completion
    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(context.getSource().getServer().getPlayerList().getPlayers()
                    .stream()
                    .map(player -> player.getName().getString()), builder);

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer sender = (ServerPlayer) source.getEntity();
        String targetName = StringArgumentType.getString(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        if (sender == null) {
            source.sendFailure(Component.literal("이 명령어는 플레이어만 사용할 수 있습니다!"));
            return Command.SINGLE_SUCCESS;
        }

        if (amount <= 0) {
            source.sendFailure(Component.literal("송금 금액은 0보다 커야 합니다!"));
            return Command.SINGLE_SUCCESS;
        }

        // 서버에서 대상 플레이어 찾기
        PlayerList playerList = source.getServer().getPlayerList();
        ServerPlayer receiver = playerList.getPlayerByName(targetName);

        if (receiver == null) {
            source.sendFailure(Component.literal("플레이어 '" + targetName + "'를 찾을 수 없습니다!"));
            return Command.SINGLE_SUCCESS;
        }

        // 자기 자신에게 송금 불가
        if (sender.equals(receiver)) {
            source.sendFailure(Component.literal("자기 자신에게 송금할 수 없습니다!"));
            return Command.SINGLE_SUCCESS;
        }

        // 스코어보드 가져오기
        Scoreboard scoreboard = sender.getScoreboard();
        String scoreName = "ansan_money";

        // 송금자 스코어 확인
        Score senderScore = scoreboard.getOrCreatePlayerScore(sender.getScoreboardName(), scoreboard.getObjective(scoreName));
        int senderBalance = senderScore.getScore();

        if (senderBalance < amount) {
            source.sendFailure(Component.literal("잔액이 부족합니다! 현재 잔액: " + senderBalance + " 안공"));
            return Command.SINGLE_SUCCESS;
        }

        // 수신자 스코어 가져오기
        Score receiverScore = scoreboard.getOrCreatePlayerScore(receiver.getScoreboardName(), scoreboard.getObjective(scoreName));

        // 송금 처리
        senderScore.setScore(senderBalance - amount);
        receiverScore.setScore(receiverScore.getScore() + amount);

        // 메시지 출력
        source.sendSuccess(() -> Component.literal("플레이어 '" + targetName + "'에게 " + amount + " 안공을 송금했습니다!"), true);
        receiver.sendSystemMessage(Component.literal("플레이어 '" + sender.getName().getString() + "'님으로부터 " + amount + " 안공을 받았습니다!"));

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("송금")
                .then(Commands.argument("player", StringArgumentType.string())
                        .suggests(PLAYER_SUGGESTIONS) // 자동 완성 추가
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(TransferCommand::execute))));
    }
}
