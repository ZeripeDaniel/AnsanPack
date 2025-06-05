package com.ansan.ansanpack.command;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.*;
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
            JobCostManager.loadFromMySQL();
            MobDropManager.loadFromMySQL();
            AnvilRecipeManager.loadFromDatabase();
            MissionManager.load();

            context.getSource().sendSuccess(() -> Component.literal("서버 설정을 MySQL에서 다시 불러왔습니다."), true);
        } catch (Exception e) {
            AnsanPack.LOGGER.error("[AnsanPack] 설정 로딩 중 오류", e);
            context.getSource().sendFailure(Component.literal("MySQL 로딩 실패: " + e.getMessage()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
