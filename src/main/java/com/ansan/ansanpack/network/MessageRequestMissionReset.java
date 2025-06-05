package com.ansan.ansanpack.network;

import com.ansan.ansanpack.mission.MissionResetService;
import com.ansan.ansanpack.mission.MissionService;
import com.ansan.ansanpack.mission.PlayerMissionData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class MessageRequestMissionReset {

    public MessageRequestMissionReset() {}

    public static void encode(MessageRequestMissionReset msg, FriendlyByteBuf buf) {}

    public static MessageRequestMissionReset decode(FriendlyByteBuf buf) {
        return new MessageRequestMissionReset();
    }

    public static void handle(MessageRequestMissionReset msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            boolean success = MissionResetService.resetDailyMissions(player.getStringUUID());
            if (success) {
                List<PlayerMissionData> missions = MissionService.getOrAssignMissions(player.getStringUUID());
                boolean canReset = MissionResetService.canResetDailyMissions(player.getStringUUID());

                MessageOpenMissionUI.sendToClient(player, missions, canReset); // ✅ 수정된 호출
            } else {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("오늘은 이미 미션을 다시 받았습니다!"),
                        false
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
