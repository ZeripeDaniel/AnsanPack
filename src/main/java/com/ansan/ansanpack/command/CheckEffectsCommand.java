package com.ansan.ansanpack.command;

import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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

public class CheckEffectsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("효과확인")
                .requires(source -> source.hasPermission(2))
                .executes(CheckEffectsCommand::execute));
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

        Properties props = UpgradeConfigManager.loadDbProps();
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" + props.getProperty("db.port") + "/" +
                props.getProperty("db.database") + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"));
             PreparedStatement stmt = conn.prepareStatement("SELECT effect_key, effect_value FROM upgrade_effects WHERE item_id = ?")) {

            stmt.setString(1, itemIdStr);
            ResultSet rs = stmt.executeQuery();

            boolean hasEffects = false;
            while (rs.next()) {
                hasEffects = true;
                String key = rs.getString("effect_key");
                float value = rs.getFloat("effect_value");
                source.sendSuccess(() -> Component.literal("🛠 효과: " + key + " = " + value), false);
            }

            if (!hasEffects) {
                source.sendSuccess(() -> Component.literal("❌ 등록된 효과가 없습니다."), false);
            }

        } catch (SQLException e) {
            source.sendFailure(Component.literal("❌ DB 오류: " + e.getMessage()));
        }

        return Command.SINGLE_SUCCESS;
    }
}
