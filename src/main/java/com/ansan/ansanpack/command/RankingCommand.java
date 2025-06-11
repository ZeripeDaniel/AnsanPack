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

        String sql = """
            SELECT cp.player_name, cp.power, lv.level
            FROM player_combat_power cp
            LEFT JOIN player_levels lv ON cp.uuid = lv.uuid
            ORDER BY cp.power DESC
            LIMIT 10
            """;

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"));
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            int rank = 1;

            source.sendSystemMessage(Component.literal("===== 전투력 랭킹 Top 10 ====="));
            while (rs.next()) {
                String name = rs.getString("player_name");
                double power = rs.getDouble("power");
                int level = rs.getInt("level");

                String line = String.format("%d위 - %s (전투력: %.2f / LV. %d)", rank, name, power, level);
                source.sendSystemMessage(Component.literal(line));
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
