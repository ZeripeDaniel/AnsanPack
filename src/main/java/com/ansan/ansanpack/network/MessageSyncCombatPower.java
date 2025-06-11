package com.ansan.ansanpack.network;

import com.ansan.ansanpack.client.level.LocalCombatPowerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSyncCombatPower {

    private final double combatPower;

    public MessageSyncCombatPower(double combatPower) {
        this.combatPower = combatPower;
    }

    public static void encode(MessageSyncCombatPower msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.combatPower);
    }

    public static MessageSyncCombatPower decode(FriendlyByteBuf buf) {
        return new MessageSyncCombatPower(buf.readDouble());
    }

    public static void handle(MessageSyncCombatPower msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> LocalCombatPowerData.update(msg.combatPower));
        ctx.get().setPacketHandled(true);
    }
}
