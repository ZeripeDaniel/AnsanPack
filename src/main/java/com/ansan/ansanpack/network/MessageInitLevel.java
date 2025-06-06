package com.ansan.ansanpack.network;

import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageInitLevel {
    private final int level;
    private final int exp;

    public MessageInitLevel(int level, int exp) {
        this.level = level;
        this.exp = exp;
    }

    public static void encode(MessageInitLevel msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level);
        buf.writeInt(msg.exp);
    }

    public static MessageInitLevel decode(FriendlyByteBuf buf) {
        return new MessageInitLevel(buf.readInt(), buf.readInt());
    }

    public static void handle(MessageInitLevel msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayerLevelData.INSTANCE.setLevel(msg.level);
            LocalPlayerLevelData.INSTANCE.setExp(msg.exp);
        });
        ctx.get().setPacketHandled(true);
    }
}
