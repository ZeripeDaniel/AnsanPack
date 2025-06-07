package com.ansan.ansanpack.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageRequestMoneyOnly {

    public static void encode(MessageRequestMoneyOnly msg, FriendlyByteBuf buf) {}

    public static MessageRequestMoneyOnly decode(FriendlyByteBuf buf) {
        return new MessageRequestMoneyOnly();
    }

    public static void handle(MessageRequestMoneyOnly msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Scoreboard board = player.getScoreboard();
            Objective obj = board.getObjective("ansan_money");
            int money = 0;
            if (obj != null) {
                money = board.getOrCreatePlayerScore(player.getScoreboardName(), obj).getScore();
            }

            // 응답 전송
            MessageSyncMoneyOnly packet = new MessageSyncMoneyOnly(money);
            com.ansan.ansanpack.AnsanPack.NETWORK.sendTo(
                    packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.get().setPacketHandled(true);
    }
}
