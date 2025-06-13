package com.ansan.ansanpack.command;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.JobCostManager;
import com.ansan.ansanpack.config.JobCostManager.JobInfo;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

import java.util.List;
import java.util.Map;

public class JobCommand {
    private static final String NBT_KEY = "ansanpack_jobs";

    private static boolean hasJob(ServerPlayer player, String job) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(NBT_KEY)) return false;
        ListTag jobList = data.getList(NBT_KEY, 8); // 8 = StringTag
        for (int i = 0; i < jobList.size(); i++) {
            if (jobList.getString(i).equals(job)) {
                return true;
            }
        }
        return false;
    }

    private static void addJob(ServerPlayer player, String job) {
        CompoundTag data = player.getPersistentData();
        ListTag jobList = data.contains(NBT_KEY) ? data.getList(NBT_KEY, 8) : new ListTag();
        jobList.add(StringTag.valueOf(job));
        data.put(NBT_KEY, jobList);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = (ServerPlayer) source.getEntity();


        if (player == null) {
            source.sendFailure(Component.literal("이 명령어는 플레이어만 사용할 수 있습니다!"));
            return Command.SINGLE_SUCCESS;
        }

        String job = StringArgumentType.getString(context, "직업");
        JobInfo jobInfo = JobCostManager.getInfo(job);

        if (job.contains(" ")) {
            source.sendFailure(Component.literal("직업명에는 공백이 포함될 수 없습니다."));
            return Command.SINGLE_SUCCESS;
        }

        if (jobInfo == null) {
            source.sendFailure(Component.literal("알 수 없는 직업이거나 설정이 존재하지 않습니다."));
            return Command.SINGLE_SUCCESS;
        }

        int requiredMoney = jobInfo.cost();
        String category = jobInfo.category();

        // 스코어보드 잔액 확인
        Scoreboard scoreboard = player.getScoreboard();
        Score score = scoreboard.getOrCreatePlayerScore(
                player.getScoreboardName(),
                scoreboard.getObjective("ansan_money"));
        int money = score.getScore();

        if (money < requiredMoney) {
            source.sendFailure(Component.literal("전직에 필요한 ansan_money가 부족합니다! (" + requiredMoney + " 필요)"));
            return Command.SINGLE_SUCCESS;
        }

        if (hasJob(player, job)) {
            source.sendFailure(Component.literal("이미 '" + job + "' 직업을 보유하고 있습니다."));
            return Command.SINGLE_SUCCESS;
        }

        String unlockCmd = "puffish_skills category unlock %s %s"
                .formatted(player.getName().getString(), category);

        int result = source.getServer().getCommands().performPrefixedCommand(
                source.getServer().createCommandSourceStack().withPermission(4), // OP 권한 포함
                unlockCmd
        );

        //AnsanPack.LOGGER.debug("[전직] puffish_skills 해금 명령 실행 결과값: {}", result);

        // 직업 추가
        addJob(player, job);

        score.setScore(money - requiredMoney);

        // 로그 출력
        AnsanPack.LOGGER.debug("[DEBUG] 전직 처리 - 플레이어: {}, 직업: {}, 비용: {}, 카테고리: {}",
                player.getName().getString(), job, requiredMoney, category);

        // 전체 유저에게 알림
        Component broadcastMsg = Component.literal("§e[전직 시스템] §f플레이어 §b"
                + player.getName().getString() + "§f님이 §a" + job + "§f으로 전직했습니다!");
        source.getServer().getPlayerList().getPlayers()
                .forEach(p -> p.sendSystemMessage(broadcastMsg));

        source.sendSuccess(() -> Component.literal("전직 완료: " + job).withStyle(ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("전직")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .then(Commands.argument("직업", StringArgumentType.greedyString())
                        .suggests((ctx, builder) ->
                                SharedSuggestionProvider.suggest(JobCostManager.getAllJobs().keySet(), builder))
                        .executes(JobCommand::execute)));
    }
}
