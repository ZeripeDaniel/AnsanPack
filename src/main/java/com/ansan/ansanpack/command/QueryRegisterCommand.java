package com.ansan.ansanpack.command;

import com.ansan.ansanpack.config.UpgradeChanceManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import java.sql.*;
import java.util.Properties;
import com.ansan.ansanpack.config.UpgradeConfigManager;

public class QueryRegisterCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("쿼리등록")
                .requires(source -> source.hasPermission(2)) // OP 권한 제한
                .then(Commands.argument("type", StringArgumentType.word())
                        .then(Commands.argument("effect_value", FloatArgumentType.floatArg(0.0f))
                                .then(Commands.argument("max_level", IntegerArgumentType.integer(1))
                                        .executes(QueryRegisterCommand::execute)))));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("이 명령어는 플레이어만 사용할 수 있습니다!"));
            return Command.SINGLE_SUCCESS;
        }

        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.isEmpty()) {
            source.sendFailure(Component.literal("손에 아이템을 들고 있어야 합니다!"));
            return Command.SINGLE_SUCCESS;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        String itemIdStr = itemId.toString();
        String type = StringArgumentType.getString(context, "type");
        float effectValue = FloatArgumentType.getFloat(context, "effect_value");
        int maxLevel = IntegerArgumentType.getInteger(context, "max_level");

        String effectKey = switch (type) {
            case "weapon" -> "damage_per_level";
            case "helmet" -> "helmet_armor";
            case "chest" -> "chest_armor";
            case "leggings" -> "leggings_armor";
            case "boots" -> "boots_armor";
            default -> {
                source.sendFailure(Component.literal("알 수 없는 타입입니다: " + type));
                yield null;
            }
        };
        if (effectKey == null) return Command.SINGLE_SUCCESS;

        Properties props = UpgradeConfigManager.loadDbProps();
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" + props.getProperty("db.port") + "/" +
                props.getProperty("db.database") + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"))) {
            // upgrades 테이블에 삽입
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO upgrades (item_id, max_level) VALUES (?, ?)")) {
                stmt.setString(1, itemIdStr);
                stmt.setInt(2, maxLevel);
                stmt.executeUpdate();
            }

            // upgrade_effects 테이블에 삽입
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO upgrade_effects (item_id, effect_key, effect_value) VALUES (?, ?, ?)")) {
                stmt.setString(1, itemIdStr);
                stmt.setString(2, effectKey);
                stmt.setFloat(3, effectValue);
                stmt.executeUpdate();
            }

            // upgrade_chance 테이블에 레벨별 확률 삽입
            float[] chances = new float[] {
                    0.9f, 0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.5f, 0.5f, 0.4f, 0.3f,
                    0.2f, 0.15f, 0.11f, 0.08f, 0.05f, 0.0f
            };
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO upgrade_chances (item_id, level, success_chance) VALUES (?, ?, ?)")) {
                for (int i = 0; i <= maxLevel; i++) {
                    float chance = i < chances.length ? chances[i] : chances[chances.length - 1];
                    stmt.setString(1, itemIdStr);
                    stmt.setInt(2, i);
                    stmt.setFloat(3, chance);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            source.sendFailure(Component.literal("데이터베이스 오류: " + e.getMessage()));
            return Command.SINGLE_SUCCESS;
        }

        UpgradeConfigManager.loadConfigFromMySQL(); // 🔁 새로 로딩
        UpgradeChanceManager.loadChancesFromMySQL();

        source.sendSuccess(() -> Component.literal("'" + itemIdStr + "' 아이템이 데이터베이스에 등록되었습니다."), true);
        return Command.SINGLE_SUCCESS;
    }
}
