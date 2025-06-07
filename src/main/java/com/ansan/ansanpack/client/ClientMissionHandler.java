// 위치: com.ansan.ansanpack.client.ClientMissionHandler
package com.ansan.ansanpack.client;

import com.ansan.ansanpack.gui.MissionScreen;
import com.ansan.ansanpack.mission.PlayerMissionData;
import com.ansan.ansanpack.network.MessageOpenMissionUI;
import com.ansan.ansanpack.network.MessageSyncMoveDistance;
import com.ansan.ansanpack.AnsanPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, value = Dist.CLIENT)
public class ClientMissionHandler {

    private static double totalDistanceMoved = 0.0;
    private static double lastX = -1, lastY = -1, lastZ = -1;

    public static void openFromInfo(List<MessageOpenMissionUI.MissionInfo> infoList, boolean canReset) {
        // GUI 열기 직전: 서버에 이동거리 패킷 전송
        int moved = consumeMovedDistance();
        if (moved > 0) {
            AnsanPack.NETWORK.sendToServer(new MessageSyncMoveDistance(moved));
        }

        List<PlayerMissionData> missions = infoList.stream().map(info -> {
            PlayerMissionData data = new PlayerMissionData(
                    "", // uuid는 클라이언트에서 필요 없음
                    info.missionId,
                    info.progress,
                    info.completed,
                    info.rewarded,
                    null
            );
            data.type = info.type;
            data.description = info.description;
            return data;
        }).toList();

        Minecraft.getInstance().setScreen(new MissionScreen(missions, canReset)); // ✅ 변경
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof LocalPlayer player)) return;

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        if (lastX >= 0) {
            double dx = x - lastX;
            double dy = y - lastY;
            double dz = z - lastZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 0.01) {
                totalDistanceMoved += dist;
            }
        }

        lastX = x;
        lastY = y;
        lastZ = z;
    }

    public static int consumeMovedDistance() {
        int result = (int) totalDistanceMoved;
        totalDistanceMoved = 0;
        return result;
    }
}
