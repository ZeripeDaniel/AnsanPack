package com.ansan.ansanpack.command;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RefreshUpgradeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("강화재계산")
                .requires(source -> source.hasPermission(2)) // OP 권한 필요 (원하면 조정 가능)
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack heldItem = player.getMainHandItem();

                    if (heldItem.isEmpty()) {
                        player.sendSystemMessage(Component.literal("❌ 손에 아이템이 없습니다."));
                        return 0;
                    }

                    if (!WeaponUpgradeSystem.canUpgrade(heldItem)) {
                        player.sendSystemMessage(Component.literal("❌ 이 아이템은 강화 대상이 아닙니다."));
                        return 0;
                    }

                    UpgradeConfigManager.getConfig(heldItem.getItem()).ifPresent(config -> {
                        int level = WeaponUpgradeSystem.getCurrentLevel(heldItem);
                        WeaponUpgradeSystem.applyEffects(heldItem.getOrCreateTag(), config, level);
                        player.sendSystemMessage(Component.literal("✅ 강화 효과 재계산 완료 (레벨: +" + level + ")"));
                    });

                    return 1;
                }));
    }
}
