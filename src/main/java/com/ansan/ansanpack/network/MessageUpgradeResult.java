package com.ansan.ansanpack.network;

import com.ansan.ansanpack.gui.UpgradeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class MessageUpgradeResult {
    private final boolean success;

    public MessageUpgradeResult(boolean success) {
        this.success = success;
    }

    public static void encode(MessageUpgradeResult msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.success);
    }

    public static MessageUpgradeResult decode(FriendlyByteBuf buffer) {
        return new MessageUpgradeResult(buffer.readBoolean());
    }

    // MessageUpgradeResult.java 26-28번 라인 수정
    public static void handle(MessageUpgradeResult msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof UpgradeScreen upgradeScreen) {
                upgradeScreen.handleUpgradeResult(msg.success);
            }
        });
        ctx.get().setPacketHandled(true);
    }


}
