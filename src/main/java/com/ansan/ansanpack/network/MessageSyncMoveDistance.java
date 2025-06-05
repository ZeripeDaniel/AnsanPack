// 위치: com.ansan.ansanpack.network.MessageSyncMoveDistance
package com.ansan.ansanpack.network;

import com.ansan.ansanpack.events.MissionEventDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSyncMoveDistance {
    private final int moved;

    public MessageSyncMoveDistance(int moved) {
        this.moved = moved;
    }

    public static void encode(MessageSyncMoveDistance msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.moved);
    }

    public static MessageSyncMoveDistance decode(FriendlyByteBuf buf) {
        return new MessageSyncMoveDistance(buf.readVarInt());
    }

    public static void handle(MessageSyncMoveDistance msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && msg.moved > 0) {
                MissionEventDispatcher.onPlayerMoved(player, msg.moved);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
