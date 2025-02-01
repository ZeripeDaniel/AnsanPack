package com.ansan.ansanpack.command;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.gui.UpgradeContainer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public class UpgradeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("upgrade")
                .requires(source -> source.hasPermission(2)) // 테스트 용이므로 op만 가능
                .executes(UpgradeCommand::executeUpgrade));
    }

    private static int executeUpgrade(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            player.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new UpgradeContainer(windowId, playerInventory),
                    Component.literal("")
            ));
            return 1;
        }
        return 0;
    }
}
