package com.ansan.ansanpack.network;

import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageGainExp {

    private final int amount;

    public MessageGainExp(int amount) {
        this.amount = amount;
    }

    public static void encode(MessageGainExp msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.amount);
    }

    public static MessageGainExp decode(FriendlyByteBuf buf) {
        return new MessageGainExp(buf.readInt());
    }

    public static void handle(MessageGainExp msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // ✅ Singleton 인스턴스를 통해 경험치 증가 처리
            LocalPlayerLevelData.INSTANCE.gainExp(msg.amount);
        });
        ctx.get().setPacketHandled(true);
    }
}
