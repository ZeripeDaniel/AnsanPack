package com.ansan.ansanpack.command;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.MissionManager;
import com.ansan.ansanpack.mission.MissionService;
import com.ansan.ansanpack.network.MessageOpenMissionUI;
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
                    var missionList = MissionService.getOrAssignMissions(player.getStringUUID());

                    // PlayerMissionData → MissionInfo 변환
                    var infoList = missionList.stream().map(data -> {
                        var def = MissionManager.getMission(data.missionId);
                        return new MessageOpenMissionUI.MissionInfo(
                                data.missionId,
                                def != null ? def.description : "(알 수 없음)",  // 🔍 description 사용
                                data.progress,
                                data.completed,
                                data.rewarded,
                                def != null ? def.type : "unknown"               // 🔍 type도 fallback 처리
                        );
                    }).toList();

                    // 클라이언트에게 전송
                    AnsanPack.NETWORK.sendTo(
                            new MessageOpenMissionUI(infoList),
                            player.connection.connection,
                            NetworkDirection.PLAY_TO_CLIENT
                    );
                    return 1;
                }));
    }
}
