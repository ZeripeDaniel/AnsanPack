package com.ansan.ansanpack.command;

import com.ansan.ansanpack.mission.MissionDB;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.stream.Collectors;

public class AddRewardCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("보상추가")
                .requires(source -> source.hasPermission(2)) // OP 권한
                .then(Commands.argument("reward_type", StringArgumentType.word())
                        .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                new String[]{"item", "exp", "money"}, b))
                        .then(Commands.argument("item_id", StringArgumentType.string())
                                .suggests((ctx, builder) ->
                                        net.minecraft.commands.SharedSuggestionProvider.suggest(
                                                ForgeRegistries.ITEMS.getKeys().stream()
                                                        .map(Objects::toString)
                                                        .collect(Collectors.toList()),
                                                builder))
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("type", StringArgumentType.word())
                                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                                        new String[]{"daily", "weekly"}, b))
                                                .executes(AddRewardCommand::execute))))));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        String rewardType = StringArgumentType.getString(context, "reward_type");
        String itemIdRaw = StringArgumentType.getString(context, "item_id");
        int value = IntegerArgumentType.getInteger(context, "value");
        String type = StringArgumentType.getString(context, "type");

        String itemId = itemIdRaw.equalsIgnoreCase("null") ? null : itemIdRaw;

        try (Connection conn = MissionDB.getConnection()) {
            String sql = "INSERT INTO mission_rewards (reward_type, reward_item, value, type) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, rewardType);
                if (itemId == null) {
                    stmt.setNull(2, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(2, itemId);
                }
                stmt.setInt(3, value);
                stmt.setString(4, type);
                stmt.executeUpdate();
            }

            context.getSource().sendSuccess(() ->
                    Component.literal("보상이 성공적으로 추가되었습니다."), true);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("보상 추가 실패: " + e.getMessage()));
        }

        return Command.SINGLE_SUCCESS;
    }
}
