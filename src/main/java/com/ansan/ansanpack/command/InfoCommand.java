package com.ansan.ansanpack.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

import java.util.List;
import java.util.stream.Collectors;

public class InfoCommand {

    private static final String NBT_KEY = "ansanpack_jobs";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("정보")
                .requires(source -> source.hasPermission(2)) // OP만 사용 가능
                .executes(InfoCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();

        for (ServerPlayer player : players) {
            String playerName = player.getName().getString();
            List<String> jobs = getJobList(player);
            int balance = getBalance(player);

            String jobText = jobs.isEmpty() ? "없음" : String.join(", ", jobs);
            String balanceText = (balance < 0) ? "없음" : balance + " 안공";

            Component message = Component.literal("")
                    .append(Component.literal("유저명: ").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(playerName).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal("\n직업: ").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(jobText).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal("\n잔액: ").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(balanceText).withStyle(ChatFormatting.GOLD));

            source.sendSuccess(() -> message, false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static List<String> getJobList(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(NBT_KEY)) return List.of();
        ListTag jobList = data.getList(NBT_KEY, 8); // 8 = StringTag
        return jobList.stream().map(tag -> tag.getAsString()).collect(Collectors.toList());
    }

    private static int getBalance(ServerPlayer player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective obj = scoreboard.getObjective("ansan_money");
        if (obj == null) return -1;
        Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), obj);
        return score.getScore();
    }
}
