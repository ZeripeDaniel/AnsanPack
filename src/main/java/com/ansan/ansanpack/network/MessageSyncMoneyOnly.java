package com.ansan.ansanpack.network;

import com.ansan.ansanpack.client.level.LocalPlayerCardData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSyncMoneyOnly {
    private final int money;

    public MessageSyncMoneyOnly(int money) {
        this.money = money;
    }

    public static void encode(MessageSyncMoneyOnly msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.money);
    }

    public static MessageSyncMoneyOnly decode(FriendlyByteBuf buf) {
        return new MessageSyncMoneyOnly(buf.readInt());
    }

    public static void handle(MessageSyncMoneyOnly msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayerCardData.INSTANCE.setMoney(msg.money);
        });
        ctx.get().setPacketHandled(true);
    }
}
