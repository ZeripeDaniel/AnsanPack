package com.ansan.ansanpack.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import com.ansan.ansanpack.config.MissionManager;

import java.util.function.Supplier;

public class MessageClaimReward {
    private final String missionId;

    public MessageClaimReward(String missionId) {
        this.missionId = missionId;
    }

    public static void encode(MessageClaimReward msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.missionId);
    }

    public static MessageClaimReward decode(FriendlyByteBuf buf) {
        return new MessageClaimReward(buf.readUtf());
    }

    public static void handle(MessageClaimReward msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 서버에서 미션 보상 수령 처리
            MissionManager.claimReward(ctx.get().getSender(), msg.missionId);
        });
        ctx.get().setPacketHandled(true);
    }
}
