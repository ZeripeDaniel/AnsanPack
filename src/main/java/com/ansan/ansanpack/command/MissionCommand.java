package com.ansan.ansanpack.command;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.MissionManager;
import com.ansan.ansanpack.mission.MissionResetService;
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

                    var infoList = missionList.stream().map(data -> {
                        var def = MissionManager.getMission(data.missionId);
                        return new MessageOpenMissionUI.MissionInfo(
                                data.missionId,
                                def != null ? def.description : "(알 수 없음)",
                                data.progress,
                                def != null ? def.goalValue : 1, // ✅ goalValue 추가
                                data.completed,
                                data.rewarded,
                                def != null ? def.type : "unknown"
                        );
                    }).toList();

                    // ✅ 다시받기 가능 여부 판단
                    boolean canReset = MissionResetService.canResetDailyMissions(player.getStringUUID());

                    // ✅ canReset 포함하여 전송
                    AnsanPack.NETWORK.sendTo(
                            new MessageOpenMissionUI(infoList, canReset),
                            player.connection.connection,
                            NetworkDirection.PLAY_TO_CLIENT
                    );
                    return 1;
                }));
    }
}
