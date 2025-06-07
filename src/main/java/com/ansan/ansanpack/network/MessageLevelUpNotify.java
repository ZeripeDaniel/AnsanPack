package com.ansan.ansanpack.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageLevelUpNotify {

    public MessageLevelUpNotify() {
        // 내용 없음
    }

    public static MessageLevelUpNotify decode(FriendlyByteBuf buf) {
        return new MessageLevelUpNotify();
    }

    public void encode(FriendlyByteBuf buf) {
        // 내용 없음
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            String command = String.format("playsound ansanpack:level_up player %s", player.getName().getString());

            player.server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withPermission(4), // 권한 부여
                    command
            );
        });
        ctx.get().setPacketHandled(true);

    }
}
