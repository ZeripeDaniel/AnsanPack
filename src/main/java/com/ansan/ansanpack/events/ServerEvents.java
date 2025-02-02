package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.network.SyncConfigPacket;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
@Mod.EventBusSubscriber(modid = AnsanPack.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        if (!server.isDedicatedServer()) return;

        // 서버 전용 경로 설정 (config/ansanpack_upgrades.json)
        Path configPath = server.getServerDirectory().toPath().resolve("config/ansanpack_upgrades.json");

        try {
            // 파일이 없으면 기본 생성
            if (!Files.exists(configPath)) {
                UpgradeConfigManager.createDefaultConfig(configPath);
                AnsanPack.LOGGER.info("기본 강화 설정 파일 생성됨: {}", configPath);
            }

            // 설정 파일 로드
            String jsonData = new String(Files.readAllBytes(configPath));
            AnsanPack.LOGGER.info("강화 설정 파일 로드 완료");

            // 모든 플레이어에게 동기화 패킷 전송
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                AnsanPack.NETWORK.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncConfigPacket(jsonData)
                );
            }

        } catch (IOException e) {
            AnsanPack.LOGGER.error("강화 설정 로드 실패!", e);
        }
    }

    public static void applyUpgrade(ItemStack item, boolean success) {
        CompoundTag tag = item.getOrCreateTag();
        int currentLevel = tag.getInt("ansan_upgrade_level");
        int newLevel = Math.max(0, success ? currentLevel + 1 : currentLevel - 1);
        tag.putInt("ansan_upgrade_level", newLevel);
        item.setTag(tag);
        item.setCount(item.getCount()); // 강제 갱신
        AnsanPack.LOGGER.debug("강화 적용: 레벨 {} → {}", currentLevel, newLevel);
    }
}