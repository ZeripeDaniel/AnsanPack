package com.ansan.ansanpack.command;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.sql.*;
import java.util.Properties;

public class AddEffectCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("효과추가")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("type", StringArgumentType.word())
                        .then(Commands.argument("effect_value", FloatArgumentType.floatArg(0.0f))
                                .executes(AddEffectCommand::execute))));
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

        String effectKey = switch (type) {
            case "attack" -> "damage_per_level";
            case "atspeed" -> "attack_spd_level";
            case "knockback" -> "knockback_level";
            case "helmet" -> "helmet_armor";
            case "chest" -> "chest_armor";
            case "leggings" -> "leggings_armor";
            case "boots" -> "boots_armor";
            case "health" -> "health_bonus";
            case "kbres" -> "resist_knockback";
            case "tough" -> "toughness_bonus";
            case "speed" -> "move_speed_bonus";
            case "luck" -> "luck_bonus";
            default -> {
                source.sendFailure(Component.literal("❌ 알 수 없는 타입입니다: " + type));
                yield null;
            }
        };

        if (effectKey == null) return Command.SINGLE_SUCCESS;

        Properties props = UpgradeConfigManager.loadDbProps();
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" + props.getProperty("db.port") + "/" +
                props.getProperty("db.database") + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"))) {

            // 먼저 upgrades에 존재하는지 확인
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM upgrades WHERE item_id = ?")) {
                checkStmt.setString(1, itemIdStr);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    source.sendFailure(Component.literal("❌ 이 아이템은 먼저 /쿼리등록 으로 등록되어야 합니다."));
                    return Command.SINGLE_SUCCESS;
                }
            }

            // 이미 effect 존재하는지 확인 후 insert/update
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM upgrade_effects WHERE item_id = ? AND effect_key = ?")) {
                checkStmt.setString(1, itemIdStr);
                checkStmt.setString(2, effectKey);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                boolean exists = rs.getInt(1) > 0;

                if (exists) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE upgrade_effects SET effect_value = ? WHERE item_id = ? AND effect_key = ?")) {
                        updateStmt.setFloat(1, effectValue);
                        updateStmt.setString(2, itemIdStr);
                        updateStmt.setString(3, effectKey);
                        updateStmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO upgrade_effects (item_id, effect_key, effect_value) VALUES (?, ?, ?)")) {
                        insertStmt.setString(1, itemIdStr);
                        insertStmt.setString(2, effectKey);
                        insertStmt.setFloat(3, effectValue);
                        insertStmt.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            source.sendFailure(Component.literal("❌ DB 오류: " + e.getMessage()));
            return Command.SINGLE_SUCCESS;
        }

        UpgradeConfigManager.loadConfigFromMySQL(); // 리로드
        source.sendSuccess(() -> Component.literal("✅ 효과 '" + effectKey + "'가 추가되었습니다."), true);
        return Command.SINGLE_SUCCESS;
    }
}
