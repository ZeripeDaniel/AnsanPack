package com.ansan.ansanpack.network;

import com.ansan.ansanpack.server.stat.PlayerStat;
import com.ansan.ansanpack.server.stat.ServerStatCache;
import com.ansan.ansanpack.config.StatDatabaseManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageRequestSaveStats {

    private final int str, agi, intel, luck, ap;

    public MessageRequestSaveStats(int str, int agi, int intel, int luck, int ap) {
        this.str = str;
        this.agi = agi;
        this.intel = intel;
        this.luck = luck;
        this.ap = ap;
    }

    public static MessageRequestSaveStats decode(FriendlyByteBuf buf) {
        return new MessageRequestSaveStats(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(str);
        buf.writeInt(agi);
        buf.writeInt(intel);
        buf.writeInt(luck);
        buf.writeInt(ap);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            PlayerStat stat = new PlayerStat(str, agi, intel, luck, ap);
            ServerStatCache.update(player.getUUID(), stat);
            StatDatabaseManager.saveStats(player.getUUID(), player.getName().getString(), stat);
        });
        ctx.get().setPacketHandled(true);
    }
}
