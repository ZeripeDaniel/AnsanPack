package com.ansan.ansanpack.network;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.gui.UpgradeContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkDirection;
import java.util.function.Supplier;

public class MessageUpgradeRequest {
    public MessageUpgradeRequest() {}

    public static void encode(MessageUpgradeRequest msg, FriendlyByteBuf buffer) {}

    public static MessageUpgradeRequest decode(FriendlyByteBuf buffer) {
        return new MessageUpgradeRequest();
    }

    public static void handle(MessageUpgradeRequest msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof UpgradeContainer container) {
                boolean result = container.upgradeItem();
                // 결과를 클라이언트에게 전송
                AnsanPack.NETWORK.sendTo(new MessageUpgradeResult(result), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
