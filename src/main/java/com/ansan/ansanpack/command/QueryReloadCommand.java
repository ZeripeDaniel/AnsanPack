package com.ansan.ansanpack.command;

import com.ansan.ansanpack.config.UpgradeChanceManager;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class QueryReloadCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("쿼리리로드")
                .requires(source -> source.hasPermission(2)) // OP 이상
                .executes(QueryReloadCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            UpgradeConfigManager.loadConfigFromMySQL();
            UpgradeChanceManager.loadChancesFromMySQL();
            context.getSource().sendSuccess(() -> Component.literal("서버 설정을 MySQL에서 다시 불러왔습니다."), true);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("MySQL 로딩 실패: " + e.getMessage()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
