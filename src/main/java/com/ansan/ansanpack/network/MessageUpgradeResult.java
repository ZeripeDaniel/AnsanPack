package com.ansan.ansanpack.network;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.events.SoundEventHandler;
import com.ansan.ansanpack.gui.UpgradeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class MessageUpgradeResult {
    public final boolean success;
    public MessageUpgradeResult(boolean success) {
        this.success = success;
    }
    public static void encode(MessageUpgradeResult msg, FriendlyByteBuf buffer) {
        AnsanPack.LOGGER.debug("[Packet] Encoding UpgradeResult: " + msg.success);
        buffer.writeBoolean(msg.success);
    }

    public static MessageUpgradeResult decode(FriendlyByteBuf buffer) {
        boolean result = buffer.readBoolean();
        AnsanPack.LOGGER.debug("Result decode upgrade result: {}", result);
        return new MessageUpgradeResult(result);
    }

    // MessageUpgradeResult.java 26-28번 라인 수정
    public static void handle(MessageUpgradeResult msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // ▼▼▼ 클라이언트 로깅 ▼▼▼
            AnsanPack.LOGGER.debug("[클라이언트] 강화 결과 수신: {}", msg.success ? "성공" : "실패");

            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().screen instanceof UpgradeScreen screen) {
                    screen.handleUpgradeResult(msg.success);
                    // ▼▼▼ GUI 업데이트 로깅 ▼▼▼
                    Minecraft.getInstance().player.inventoryMenu.broadcastChanges();
                    AnsanPack.LOGGER.debug("강화 결과 화면 업데이트 완료");
                }
                SoundEventHandler.handleUpgradeSound(msg, ctx.get());
                // ▼▼▼ 사운드 재생 로깅 ▼▼▼
                AnsanPack.LOGGER.debug("사운드 재생: {}", msg.success ? "폭죽" : "TNT");
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.inventoryMenu.broadcastChanges();
            });
        });
        ctx.get().setPacketHandled(true);
    }



}
