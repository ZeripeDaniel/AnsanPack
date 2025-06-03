package com.ansan.ansanpack.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class MyInfoCommand {
    private static final String NBT_KEY = "ansanpack_jobs";

    private static int execute(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("이 명령어는 플레이어만 사용할 수 있습니다."));
            return Command.SINGLE_SUCCESS;
        }

        // === 직업 정보 가져오기 ===
        CompoundTag data = player.getPersistentData();
        List<String> jobNames = new ArrayList<>();

        if (data.contains(NBT_KEY)) {
            ListTag jobList = data.getList(NBT_KEY, Tag.TAG_STRING); // 8
            for (int i = 0; i < jobList.size(); i++) {
                jobNames.add(jobList.getString(i));
            }
        }

        String jobLine = jobNames.isEmpty()
                ? "직업 정보가 없습니다."
                : "직업: " + String.join(", ", jobNames);

        // === 예금 (잔액) 가져오기 ===
        Scoreboard scoreboard = player.getScoreboard();
        String scoreName = "ansan_money";
        Score score = scoreboard.getOrCreatePlayerScore(
                player.getScoreboardName(), scoreboard.getObjective(scoreName));
        int balance = score.getScore();

        // === 출력 ===
        source.sendSuccess(() -> Component.literal(jobLine).withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> Component.literal("예금: " + balance + " 안공").withStyle(ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("내정보")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .executes(ctx -> execute(ctx.getSource())));
    }
}
