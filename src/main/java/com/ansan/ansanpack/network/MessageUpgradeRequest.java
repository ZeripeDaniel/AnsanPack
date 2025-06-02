package com.ansan.ansanpack.network;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.gui.UpgradeContainer;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkDirection;
import java.util.function.Supplier;

public class MessageUpgradeRequest {
    private final int upgradeSlotIndex;
    private final int stoneSlotIndex;


    public MessageUpgradeRequest(int upgradeSlotIndex, int stoneSlotIndex) {
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.stoneSlotIndex = stoneSlotIndex;
    }

    public static void encode(MessageUpgradeRequest msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.upgradeSlotIndex);
        buffer.writeInt(msg.stoneSlotIndex);
        AnsanPack.LOGGER.debug("서버 수신: UpgradeRequest: {}, {}", msg.upgradeSlotIndex, msg.stoneSlotIndex);
    }

    public static MessageUpgradeRequest decode(FriendlyByteBuf buf) {
        if (buf.readableBytes() < 8) {
            throw new IllegalArgumentException("패킷 데이터가 너무 짧습니다!");
        }

        int idx1 = buf.readInt();
        int idx2 = buf.readInt();

        AnsanPack.LOGGER.debug("Decoding upgrade request: {}, {}", idx1, idx2);
        return new MessageUpgradeRequest(idx1, idx2); // ✅ 올바르게 전달
    }

    public static void handle(MessageUpgradeRequest msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !(player.containerMenu instanceof UpgradeContainer container)) return;

            try {
                ItemStack weapon = container.getSlot(msg.upgradeSlotIndex).getItem();
                ItemStack stone = container.getSlot(msg.stoneSlotIndex).getItem();

                if (!weapon.isEmpty() && !stone.isEmpty()) {
                    boolean result = WeaponUpgradeSystem.tryUpgrade(weapon, stone);

                    stone.shrink(1); // 성공/실패 관계없이 소모
                    container.getSlot(msg.stoneSlotIndex).setChanged();

                    AnsanPack.NETWORK.sendTo(
                            new MessageUpgradeResult(result),
                            player.connection.connection,
                            NetworkDirection.PLAY_TO_CLIENT
                    );
                }
            } catch (IndexOutOfBoundsException e) {
                AnsanPack.LOGGER.error("슬롯 인덱스 오류: {}", e.getMessage());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
