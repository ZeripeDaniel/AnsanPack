package com.ansan.ansanpack.network;

import com.ansan.ansanpack.gui.MissionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageRewardResult {
    private final String missionId;

    public MessageRewardResult(String missionId) {
        this.missionId = missionId;
    }

    public static void encode(MessageRewardResult msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.missionId);
    }

    public static MessageRewardResult decode(FriendlyByteBuf buf) {
        return new MessageRewardResult(buf.readUtf());
    }

    public static void handle(MessageRewardResult msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof MissionScreen screen) {
                screen.markMissionRewarded(msg.missionId); // 🔧 보상 받은 미션을 표시
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
