//package com.ansan.ansanpack.command;
//
//import com.mojang.brigadier.Command;
//import com.mojang.brigadier.CommandDispatcher;
//import com.mojang.brigadier.arguments.IntegerArgumentType;
//import com.mojang.brigadier.arguments.StringArgumentType;
//import com.mojang.brigadier.context.CommandContext;
//import net.minecraft.commands.CommandSourceStack;
//import net.minecraft.commands.Commands;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.server.players.PlayerList;
//import net.minecraft.world.scores.Score;
//import net.minecraft.world.scores.Scoreboard;
//
//public class GiveMoneyCommand {
//
//    private static int execute(CommandContext<CommandSourceStack> context) {
//        CommandSourceStack source = context.getSource();
//        String targetName = StringArgumentType.getString(context, "player");
//        int amount = IntegerArgumentType.getInteger(context, "amount");
//
//        // 금액 확인
//        if (amount <= 0) {
//            source.sendFailure(Component.literal("금액은 0보다 커야 합니다!"));
//            return Command.SINGLE_SUCCESS;
//        }
//
//        // 서버에서 대상 플레이어 찾기
//        PlayerList playerList = source.getServer().getPlayerList();
//        ServerPlayer targetPlayer = playerList.getPlayerByName(targetName);
//
//        if (targetPlayer == null) {
//            source.sendFailure(Component.literal("플레이어 '" + targetName + "'를 찾을 수 없습니다!"));
//            return Command.SINGLE_SUCCESS;
//        }
//
//        // 스코어보드에 금액 추가
//        Scoreboard scoreboard = targetPlayer.getScoreboard();
//        String scoreName = "ansan_money";
//        Score score = scoreboard.getOrCreatePlayerScore(targetPlayer.getScoreboardName(), scoreboard.getObjective(scoreName));
//        score.setScore(score.getScore() + amount);
//
//        // 성공 메시지
//        source.sendSuccess(
//                () -> Component.literal("플레이어 '" + targetName + "'에게 " + amount + " 안공을 추가했습니다!"), true
//        );
//        targetPlayer.sendSystemMessage(
//                Component.literal("당신의 잔액이 " + amount + " 안공 추가되었습니다!")
//        );
//
//        return Command.SINGLE_SUCCESS;
//    }
//
//    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
//        dispatcher.register(Commands.literal("givemoney")
//                .requires(source -> source.hasPermission(4)) // OP 권한 필요
//                .then(Commands.argument("player", StringArgumentType.string())
//                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
//                                .executes(GiveMoneyCommand::execute))));
//    }
//}
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

public class GiveMoneyCommand {

    // SuggestionProvider to list all online players
    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(context.getSource().getServer().getPlayerList().getPlayers()
                    .stream()
                    .map(player -> player.getName().getString()), builder);

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String targetName = StringArgumentType.getString(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        if (amount <= 0) {
            source.sendFailure(Component.literal("금액은 0보다 커야 합니다!"));
            return Command.SINGLE_SUCCESS;
        }

        // 서버에서 대상 플레이어 찾기
        PlayerList playerList = source.getServer().getPlayerList();
        ServerPlayer targetPlayer = playerList.getPlayerByName(targetName);

        if (targetPlayer == null) {
            source.sendFailure(Component.literal("플레이어 '" + targetName + "'를 찾을 수 없습니다!"));
            return Command.SINGLE_SUCCESS;
        }

        // 스코어보드에 금액 추가
        Scoreboard scoreboard = targetPlayer.getScoreboard();
        String scoreName = "ansan_money";
        Score score = scoreboard.getOrCreatePlayerScore(targetPlayer.getScoreboardName(), scoreboard.getObjective(scoreName));
        score.setScore(score.getScore() + amount);

        // 메시지 출력
        source.sendSuccess(
                () -> Component.literal("플레이어 '" + targetName + "'에게 " + amount + " 안공을 추가했습니다!"), true
        );
        targetPlayer.sendSystemMessage(
                Component.literal("당신의 잔액이 " + amount + " 안공 추가되었습니다!")
        );

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("givemoney")
                .requires(source -> source.hasPermission(2)) // OP 권한 필요
                .then(Commands.argument("player", StringArgumentType.string())
                        .suggests(PLAYER_SUGGESTIONS) // 자동 완성 추가
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(GiveMoneyCommand::execute))));
    }
}

