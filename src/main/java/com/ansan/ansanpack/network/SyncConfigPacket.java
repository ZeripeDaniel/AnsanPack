package com.ansan.ansanpack.network;

import com.ansan.ansanpack.AnsanPack;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.ansan.ansanpack.config.UpgradeConfigManager;
public class SyncConfigPacket {
    private final String jsonData;

    public SyncConfigPacket(String jsonData) {
        // JSON 데이터 크기 검증
        if (jsonData.length() > 32000) {
            throw new IllegalArgumentException("JSON data too large!");
        }
        this.jsonData = jsonData;
    }

    // SyncConfigPacket.java 수정
    // 압축 추가 예시 (GZIP)
    public static void encode(SyncConfigPacket msg, FriendlyByteBuf buffer) {
        byte[] jsonBytes = msg.jsonData.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(jsonBytes);
        } catch (IOException e) {
            AnsanPack.LOGGER.error("Failed to compress JSON data", e);
            // 압축 실패 시 비압축 데이터 전송
            buffer.writeInt(jsonBytes.length);
            buffer.writeBytes(jsonBytes);
            return;
        }
        byte[] compressed = bos.toByteArray();
        buffer.writeInt(compressed.length);
        buffer.writeBytes(compressed);
    }

    public static SyncConfigPacket decode(FriendlyByteBuf buffer) {
        int length = buffer.readInt();
        byte[] compressed = new byte[length];
        buffer.readBytes(compressed);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            byte[] jsonBytes = gzip.readAllBytes();
            return new SyncConfigPacket(new String(jsonBytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            AnsanPack.LOGGER.error("Failed to decompress JSON data", e);
            // 압축 해제 실패 시 원본 데이터 사용
            return new SyncConfigPacket(new String(compressed, StandardCharsets.UTF_8));
        }
    }
    public static void handle(SyncConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                AnsanPack.LOGGER.info("리시브 json 데이터 싱크컨피그패킷: {}", msg.jsonData.length());
                UpgradeConfigManager.loadConfigFromString(msg.jsonData);
            } catch (Exception e) {
                AnsanPack.LOGGER.error("패킷 처리 실패", e);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
