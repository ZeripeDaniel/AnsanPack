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
    private final ItemStack weapon;
    private final ItemStack stone;
    public MessageUpgradeRequest(ItemStack weapon, ItemStack stone) {
        this.weapon = weapon;
        this.stone = stone;
    }

    public static void encode(MessageUpgradeRequest msg, FriendlyByteBuf buffer) {
        buffer.writeItem(msg.weapon); // 아이템 스택 직렬화
        buffer.writeItem(msg.stone);
    }


    public static MessageUpgradeRequest decode(FriendlyByteBuf buffer) {
        return new MessageUpgradeRequest(
                buffer.readItem(), // 아이템 스택 역직렬화
                buffer.readItem()
        );
    }

    public static void handle(MessageUpgradeRequest msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // ▼▼▼ 로깅 추가 ▼▼▼
                AnsanPack.LOGGER.info("[강화 시도] 플레이어: {}, 아이템: {}, 강화석: {}",
                        player.getName().getString(),
                        msg.weapon.getDisplayName().getString(),
                        msg.stone.getDisplayName().getString()
                );

                boolean result = WeaponUpgradeSystem.tryUpgrade(msg.weapon, msg.stone);

                // ▼▼▼ 결과 로깅 ▼▼▼
                AnsanPack.LOGGER.info("[강화 결과] 플레이어: {}, 성공 여부: {}",
                        player.getName().getString(),
                        result ? "성공" : "실패"
                );

                AnsanPack.NETWORK.sendTo(
                        new MessageUpgradeResult(result),
                        player.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT
                );

            }
        });
        ctx.get().setPacketHandled(true);
    }

}
