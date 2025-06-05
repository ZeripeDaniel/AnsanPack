// 위치: com.ansan.ansanpack.client.ClientMissionHandler
package com.ansan.ansanpack.client;

import com.ansan.ansanpack.gui.MissionScreen;
import com.ansan.ansanpack.mission.PlayerMissionData;
import com.ansan.ansanpack.network.MessageOpenMissionUI;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ClientMissionHandler {

    public static void openFromInfo(List<MessageOpenMissionUI.MissionInfo> infoList) {
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

        Minecraft.getInstance().setScreen(new MissionScreen(missions));
    }
}
