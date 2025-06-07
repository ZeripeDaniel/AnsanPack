package com.ansan.ansanpack.network;

import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageInitLevel {
    private final int level;
    private final double exp;

    public MessageInitLevel(int level, double exp) {
        this.level = level;
        this.exp = exp;
    }

    public static void encode(MessageInitLevel msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level);
        buf.writeDouble(msg.exp);
    }

    public static MessageInitLevel decode(FriendlyByteBuf buf) {
        return new MessageInitLevel(buf.readInt(), buf.readDouble());
    }

    public static void handle(MessageInitLevel msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayerLevelData.INSTANCE.reset(); // 기존 값 제거
            LocalPlayerLevelData.INSTANCE.setLevel(msg.level);
            LocalPlayerLevelData.INSTANCE.setExp(msg.exp);
        });
        ctx.get().setPacketHandled(true);
    }
}
