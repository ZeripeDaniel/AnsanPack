package com.ansan.ansanpack.network;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.LevelDatabaseManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageRequestSaveLevel {

    private final int level;
    private final double exp;

    public MessageRequestSaveLevel(int level, double exp) {
        this.level = level;
        this.exp = exp;
    }

    public static void encode(MessageRequestSaveLevel msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level);
        buf.writeDouble(msg.exp);
    }

    public static MessageRequestSaveLevel decode(FriendlyByteBuf buf) {
        return new MessageRequestSaveLevel(buf.readInt(), buf.readDouble());
    }

    public static void handle(MessageRequestSaveLevel msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                AnsanPack.LOGGER.info("[레벨 저장 요청] {} (UUID: {}) → level={}, exp={}", player.getName().getString(), player.getUUID(), msg.level, msg.exp);
                LevelDatabaseManager.saveOrUpdate(player.getUUID(), player.getName().getString(), msg.level, msg.exp);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
