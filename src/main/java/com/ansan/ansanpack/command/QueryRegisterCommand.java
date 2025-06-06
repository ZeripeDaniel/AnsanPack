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
            // 무기
            case "attack" -> "damage_per_level";
            case "atspeed" -> "attack_spd_level";
            case "knockback" -> "knockback_level";

            // 방어구
            case "helmet" -> "helmet_armor";
            case "chest" -> "chest_armor";
            case "leggings" -> "leggings_armor";
            case "boots" -> "boots_armor";

            // 공통/기타
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
            //구식버전
//            float[] chances = new float[] {
//                    0.7f, 0.6f, 0.55f, 0.5f, 0.45f, 0.4f, 0.35f, 0.3f, 0.25f, 0.2f,
//                    0.15f, 0.11f, 0.08f, 0.05f, 0.03f, 0.0f
//            };
            //신식버전 자동계산.
            float[] chances = generateChances(maxLevel);

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
    private static float[] generateChances(int maxLevel) {
        float[] chances = new float[maxLevel + 1];

        if (maxLevel < 1) {
            chances[0] = 0.0f;
            return chances;
        }

        for (int i = 0; i < maxLevel; i++) {
            float chance;

            if (i <= 9) {
                // 0~9강: 70% → 10%
                chance = 0.7f - (0.6f / 9f) * i;
            } else if (i <= 14) {
                // 10~14강: 10% → 6% 부드럽게 하강
                chance = 0.10f - (0.04f / 4f) * (i - 10);  // i=10 → 10%, i=14 → 6%
            } else {
                // 15~(maxLevel-1): 5% → 1% 선형 하강
                int hardLevels = maxLevel - 15;
                float ratio = (float)(i - 15) / Math.max(1, hardLevels);  // 방어적 처리
                chance = 0.05f - 0.04f * ratio; // i=15 → 5%, i=max-1 → 1%
            }

            // 마지막 전 단계는 무조건 최소 1%
            if (i == maxLevel - 1) {
                chance = Math.max(0.01f, chance);
            }

            // 1% 하한 유지, 마지막 전 외에는 1% 미만 허용 가능
            if (i < maxLevel - 1) {
                chance = Math.max(0.005f, chance);
            }

            // 10% 초과 방지
            chance = Math.min(chance, 0.10f);

            // 반올림
            chances[i] = Math.round(chance * 100f) / 100f;
        }

        // 마지막 강화는 0%
        chances[maxLevel] = 0.0f;

        return chances;
    }


}
