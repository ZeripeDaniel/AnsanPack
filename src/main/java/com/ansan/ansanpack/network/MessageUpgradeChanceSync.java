package com.ansan.ansanpack.network;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.gui.UpgradeScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageUpgradeChanceSync {
    private final String itemId;
    private final int level;
    private final double chance;

    public MessageUpgradeChanceSync(String itemId, int level, double chance) {
        this.itemId = itemId;
        this.level = level;
        this.chance = chance;
    }

    public static void encode(MessageUpgradeChanceSync msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.itemId);
        buf.writeInt(msg.level);
        buf.writeDouble(msg.chance);
    }

    public static MessageUpgradeChanceSync decode(FriendlyByteBuf buf) {
        return new MessageUpgradeChanceSync(
                buf.readUtf(),
                buf.readInt(),
                buf.readDouble()
        );
    }

    public static void handle(MessageUpgradeChanceSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            AnsanPack.LOGGER.debug("[DEBUG] 클라이언트 수신 - 아이템: {}, 레벨: {}, 확률: {}",
                    msg.itemId, msg.level, msg.chance);

            UpgradeScreen.setChance(msg.itemId, msg.level, msg.chance);
        });
        ctx.get().setPacketHandled(true);
    }


    @OnlyIn(Dist.CLIENT)
    private static void handleClient(String itemId, int level, double chance) {
        UpgradeScreen.setChance(itemId, level, chance);
    }
}
