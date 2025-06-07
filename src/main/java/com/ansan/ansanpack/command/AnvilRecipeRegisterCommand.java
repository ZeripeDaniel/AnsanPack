package com.ansan.ansanpack.command;

import com.ansan.ansanpack.config.AnvilRecipeManager;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.ForgeRegistries;

import java.sql.*;
import java.util.Properties;

public class AnvilRecipeRegisterCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("anvilregister")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("insert_item", StringArgumentType.word())
                        .suggests(ITEM_ID_SUGGESTIONS)
                        .then(Commands.argument("resource_item", StringArgumentType.word())
                                .suggests(ITEM_ID_SUGGESTIONS)
                                .then(Commands.argument("stack", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("result_item", StringArgumentType.word())
                                                .suggests(ITEM_ID_SUGGESTIONS)
                                                .then(Commands.argument("cost_level", IntegerArgumentType.integer(1))
                                                        .executes(AnvilRecipeRegisterCommand::execute)))))));
    }

    // SuggestionProvider 등록
    private static final SuggestionProvider<CommandSourceStack> ITEM_ID_SUGGESTIONS = (context, builder) -> {
        ForgeRegistries.ITEMS.getKeys().stream()
                .map(ResourceLocation::toString)
                .filter(id -> id.startsWith(builder.getRemaining())) // 자동완성 중복 방지
                .forEach(builder::suggest);
        return builder.buildFuture();
    };
    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return Command.SINGLE_SUCCESS;
        }

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            source.sendFailure(Component.literal("아이템을 손에 들어주세요."));
            return Command.SINGLE_SUCCESS;
        }

        ResourceLocation insertId = ForgeRegistries.ITEMS.getKey(held.getItem());
        String resourceId = StringArgumentType.getString(context, "resource_item");
        int stack = IntegerArgumentType.getInteger(context, "stack");
        String resultId = StringArgumentType.getString(context, "result_item");
        int cost = IntegerArgumentType.getInteger(context, "cost_level");

        Properties props = UpgradeConfigManager.loadDbProps();
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" + props.getProperty("db.port") + "/" +
                props.getProperty("db.database") + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"));
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO anvil_recipes (insert_item, resource_item, stack, result_item, cost_level) VALUES (?, ?, ?, ?, ?)")) {

            stmt.setString(1, insertId.toString());
            stmt.setString(2, resourceId);
            stmt.setInt(3, stack);
            stmt.setString(4, resultId);
            stmt.setInt(5, cost);
            stmt.executeUpdate();

        } catch (SQLException e) {
            source.sendFailure(Component.literal("DB 오류: " + e.getMessage()));
            return Command.SINGLE_SUCCESS;
        }

        AnvilRecipeManager.loadFromDatabase(); // 🔁 즉시 반영
        source.sendSuccess(() -> Component.literal("모루 레시피가 등록되었습니다: " + insertId), true);
        return Command.SINGLE_SUCCESS;
    }
}
