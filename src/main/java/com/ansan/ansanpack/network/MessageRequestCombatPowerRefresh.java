package com.ansan.ansanpack.network;

import com.ansan.ansanpack.common.CombatPowerCalculator;
import com.ansan.ansanpack.AnsanPack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class MessageRequestCombatPowerRefresh {
    public static MessageRequestCombatPowerRefresh decode(FriendlyByteBuf buf) {
        return new MessageRequestCombatPowerRefresh();
    }

    public static void encode(MessageRequestCombatPowerRefresh msg, FriendlyByteBuf buf) {
        // nothing to encode
    }

    public static void handle(MessageRequestCombatPowerRefresh msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                double power = CombatPowerCalculator.calculate(player);
                AnsanPack.NETWORK.send(PacketDistributor.PLAYER.with(() -> player), new MessageSyncCombatPower(power));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
