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
    }

    public static MessageUpgradeRequest decode(FriendlyByteBuf buffer) {
        return new MessageUpgradeRequest(buffer.readInt(), buffer.readInt());
    }

    public static void handle(MessageUpgradeRequest msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof UpgradeContainer container) {
                // 슬롯 인덱스로 실제 아이템 가져오기
                ItemStack weapon = container.getSlot(msg.upgradeSlotIndex).getItem();
                ItemStack stone = container.getSlot(msg.stoneSlotIndex).getItem();

                if (!weapon.isEmpty() && !stone.isEmpty()) {
                    boolean result = WeaponUpgradeSystem.tryUpgrade(weapon, stone);

                    // 강화석 소모
                    if (result) {
                        stone.shrink(1);
                        container.getSlot(msg.stoneSlotIndex).setChanged();
                    }

                    // 결과 전송
                    AnsanPack.NETWORK.sendTo(
                            new MessageUpgradeResult(result),
                            player.connection.connection,
                            NetworkDirection.PLAY_TO_CLIENT
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }


    private static boolean isValidUpgrade(ItemStack weapon, ItemStack stone) {
        return !weapon.isEmpty() && !stone.isEmpty() && weapon.isDamageableItem();
    }
}
