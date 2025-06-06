package com.ansan.ansanpack.network;

import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import com.ansan.ansanpack.server.stat.ServerStatCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 서버 → 클라이언트: 스탯 초기값 동기화
 */
public class MessageSyncStats {

    private final int str, agi, intel, luck, ap;

    public MessageSyncStats(int str, int agi, int intel, int luck, int ap) {
        this.str = str;
        this.agi = agi;
        this.intel = intel;
        this.luck = luck;
        this.ap = ap;
    }

    public static MessageSyncStats decode(FriendlyByteBuf buf) {
        return new MessageSyncStats(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(str);
        buf.writeInt(agi);
        buf.writeInt(intel);
        buf.writeInt(luck);
        buf.writeInt(ap);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(this::handleClient);
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        LocalPlayerStatData.INSTANCE.loadFromSQL(str, agi, intel, luck, ap);
    }
}
