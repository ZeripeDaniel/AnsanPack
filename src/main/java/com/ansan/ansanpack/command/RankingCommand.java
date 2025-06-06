package com.ansan.ansanpack.command;

import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.sql.*;
import java.util.Properties;

public class RankingCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("랭킹")
                        .requires(source -> source.hasPermission(0))
                        .executes(ctx -> execute(ctx.getSource()))
        );
    }

    private static int execute(CommandSourceStack source) {
        Properties props = UpgradeConfigManager.loadDbProps();
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" + props.getProperty("db.port") + "/" +
                props.getProperty("db.database") + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"));
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT player_name, level FROM player_levels ORDER BY level DESC LIMIT 10")) {

            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            source.sendSystemMessage(Component.literal("===== 안산 레벨 랭킹 Top 10 ====="));
            while (rs.next()) {
                String name = rs.getString("player_name");
                int level = rs.getInt("level");
                source.sendSystemMessage(Component.literal(rank + "위 - " + name + " (LV. " + level + ")"));
                rank++;
            }
            if (rank == 1) {
                source.sendSystemMessage(Component.literal("랭킹 데이터가 없습니다."));
            }

        } catch (SQLException e) {
            source.sendSystemMessage(Component.literal("랭킹 정보를 불러오는 중 오류가 발생했습니다."));
            e.printStackTrace();
        }

        return Command.SINGLE_SUCCESS;
    }
}
