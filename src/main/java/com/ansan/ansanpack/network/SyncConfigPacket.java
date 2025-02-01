package com.ansan.ansanpack.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import com.ansan.ansanpack.config.UpgradeConfigManager;
public class SyncConfigPacket {
    private final String jsonData;

    public SyncConfigPacket(String jsonData) {
        this.jsonData = jsonData;
    }

    public static void encode(SyncConfigPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.jsonData);
    }

    public static SyncConfigPacket decode(FriendlyByteBuf buffer) {
        return new SyncConfigPacket(buffer.readUtf());
    }

    public static void handle(SyncConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 클라이언트 측 JSON 파싱
            UpgradeConfigManager.loadConfigFromString(msg.jsonData);
        });
        ctx.get().setPacketHandled(true);
    }
}
