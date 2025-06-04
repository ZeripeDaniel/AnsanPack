package com.ansan.ansanpack.command;

import com.ansan.ansanpack.mission.MissionService;
import com.ansan.ansanpack.network.MessageOpenMissionUI;
import com.ansan.ansanpack.AnsanPack;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

public class MissionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("미션")
                .requires(src -> src.getEntity() instanceof ServerPlayer)
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayer();
                    var list = MissionService.getOrAssignMissions(player.getStringUUID());

                    // 클라이언트에게 미션 목록 전송
                    AnsanPack.NETWORK.sendTo(
                            new MessageOpenMissionUI(list), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT
                    );
                    return 1;
                }));
    }
}
