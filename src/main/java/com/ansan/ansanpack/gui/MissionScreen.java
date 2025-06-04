package com.ansan.ansanpack.gui;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.mission.PlayerMissionData;
import com.ansan.ansanpack.network.MessageClaimReward;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MissionScreen extends Screen {
    private final List<PlayerMissionData> missions;

    public static void open(List<PlayerMissionData> missions) {
        Minecraft.getInstance().setScreen(new MissionScreen(missions));
    }

    public MissionScreen(List<PlayerMissionData> missions) {
        super(Component.literal("미션 목록"));
        this.missions = missions;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 120;
        int y = this.height / 2 - 70;

        for (int i = 0; i < missions.size(); i++) {
            PlayerMissionData mission = missions.get(i);
            int yOffset = y + i * 25;

            // 미션 텍스트 구성
            String label = "[" + mission.missionId + "] " +
                    mission.progress + "% " +
                    (mission.completed ? "✅" : "") +
                    (mission.rewarded ? "🎁완료" : "");

            // 버튼 생성
            Button button = Button.builder(
                    Component.literal(label),
                    btn -> {
                        // 보상 수령 가능 시 서버로 패킷 전송
                        if (mission.completed && !mission.rewarded) {
                            AnsanPack.NETWORK.sendToServer(new MessageClaimReward(mission.missionId));
                        }
                    }
            ).pos(x, yOffset).size(240, 20).build();

            this.addRenderableWidget(button);
        }
    }

    public void markMissionRewarded(String missionId) {
        for (PlayerMissionData mission : missions) {
            if (mission.missionId.equals(missionId)) {
                mission.rewarded = true;
                break;
            }
        }
        this.init(); // UI 다시 초기화해서 버튼 갱신
    }
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        super.render(g, mx, my, pt);
        g.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}
