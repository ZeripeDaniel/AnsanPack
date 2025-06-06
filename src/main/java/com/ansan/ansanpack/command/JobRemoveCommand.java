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

import java.util.stream.Collectors;

public class JobRemoveCommand {

    private static final String NBT_KEY = "ansanpack_jobs";

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        String job = StringArgumentType.getString(context, "job");

        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.literal("유효하지 않은 플레이어입니다."));
            return Command.SINGLE_SUCCESS;
        }

        if (job.contains(" ")) {
            AnsanPack.LOGGER.debug("[WARN] 직업명에 공백 포함: " + job);
        }

        JobInfo jobInfo = JobCostManager.getInfo(job);
        if (jobInfo == null) {
            source.sendFailure(Component.literal("알 수 없는 직업이거나 설정이 존재하지 않습니다."));
            return Command.SINGLE_SUCCESS;
        }

        CompoundTag data = player.getPersistentData();
        ListTag jobList = data.contains(NBT_KEY, 9) ? data.getList(NBT_KEY, 8) : new ListTag();

        boolean removed = false;
        ListTag newList = new ListTag();
        for (int i = 0; i < jobList.size(); i++) {
            StringTag tag = (StringTag) jobList.get(i);
            if (tag.getAsString().equals(job)) {
                removed = true;
                continue;
            }
            newList.add(tag);
        }

        if (!removed) {
            source.sendFailure(Component.literal("플레이어는 해당 직업을 보유하고 있지 않습니다."));
            return Command.SINGLE_SUCCESS;
        }

        data.put(NBT_KEY, newList);
        String category = jobInfo.category();

        String unlockCmd = "puffish_skills category lock %s %s"
                .formatted(player.getName().getString(), category);
        source.getServer().getCommands().performPrefixedCommand(source.withSuppressedOutput(), unlockCmd);

        AnsanPack.LOGGER.debug("[DEBUG] 직업 삭제 - 플레이어: {}, 직업: {}", player.getName().getString(), job);

        Component broadcastMsg = Component.literal("§e[전직 시스템] §f플레이어 §b"
                + player.getName().getString() + "§f님이 §c" + job + "§f 직업을 삭제했습니다!");
        source.getServer().getPlayerList().getPlayers()
                .forEach(p -> p.sendSystemMessage(broadcastMsg));

        source.sendSuccess(() -> Component.literal("직업 삭제 완료: " + job).withStyle(ChatFormatting.RED), true);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("직업삭제")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    var players = ctx.getSource().getServer().getPlayerList().getPlayers();
                                    return SharedSuggestionProvider.suggest(
                                            players.stream().map(p -> p.getName().getString()).collect(Collectors.toList()),
                                            builder
                                    );
                                })
                                .then(Commands.argument("job", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) ->
                                                SharedSuggestionProvider.suggest(JobCostManager.getAllJobs().keySet(), builder))
                                        .executes(JobRemoveCommand::execute)))
        );
    }
}
