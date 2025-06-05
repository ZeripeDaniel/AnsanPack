package com.ansan.ansanpack.gui;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.MissionManager;
import com.ansan.ansanpack.mission.PlayerMissionData;
import com.ansan.ansanpack.network.MessageClaimReward;
import com.ansan.ansanpack.network.MessageOpenMissionUI;
import com.ansan.ansanpack.network.MessageRequestMissionReset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

public class MissionScreen extends Screen {
    private final List<PlayerMissionData> missions;
    private final List<PlayerMissionData> dailyMissions;
    private final List<PlayerMissionData> weeklyMissions;
    private final boolean canReset; // ✅ 추가

    public static void open(List<PlayerMissionData> missions, boolean canReset) {
        Minecraft.getInstance().setScreen(new MissionScreen(missions, canReset));
    }

    public MissionScreen(List<PlayerMissionData> missions, boolean canReset) {
        super(Component.literal("미션 목록"));
        this.missions = missions;
        this.canReset = canReset;
        this.dailyMissions = missions.stream().filter(m -> "daily".equals(m.type)).collect(Collectors.toList());
        this.weeklyMissions = missions.stream().filter(m -> "weekly".equals(m.type)).collect(Collectors.toList());
    }

    @Override
    protected void init() {
        this.clearWidgets();

        int leftX = this.width / 2 - 130;
        int rightX = this.width / 2 + 10;
        int startY = this.height / 2 - 70;

        // 일일 미션 버튼
        for (int i = 0; i < dailyMissions.size(); i++) {
            PlayerMissionData mission = dailyMissions.get(i);
            int yOffset = startY + i * 25;

            int goal = mission.goalValue > 0 ? mission.goalValue : 1;
            int progress = mission.progress;
            int percent = (int)((progress * 100.0) / goal);

            String label = mission.description + " " +
                    progress + " / " + goal + " (" + percent + "%) " +
                    (mission.completed ? "✅" : "") +
                    (mission.rewarded ? "🎁" : "");

            Button button = Button.builder(
                    Component.literal(label),
                    btn -> tryClaim(mission)
            ).pos(leftX, yOffset).size(120, 20).build();

            this.addRenderableWidget(button);
        }

        // 주간 미션 버튼
        for (int i = 0; i < weeklyMissions.size(); i++) {
            PlayerMissionData mission = weeklyMissions.get(i);
            int yOffset = startY + i * 25;

            int goal = mission.goalValue > 0 ? mission.goalValue : 1;
            int progress = mission.progress;
            int percent = (int)((progress * 100.0) / goal);

            String label = mission.description + " " +
                    progress + " / " + goal + " (" + percent + "%) " +
                    (mission.completed ? "✅" : "") +
                    (mission.rewarded ? "🎁" : "");

            Button button = Button.builder(
                    Component.literal(label),
                    btn -> tryClaim(mission)
            ).pos(rightX, yOffset).size(120, 20).build();

            this.addRenderableWidget(button);
        }

        int btnY = startY + Math.max(dailyMissions.size(), weeklyMissions.size()) * 25 + 20;
        int centerX = this.width / 2 - 60;

        // 다시받기 버튼
        Button resetBtn = Button.builder(Component.literal("다시받기"), btn -> {
            AnsanPack.NETWORK.sendToServer(new MessageRequestMissionReset());
        }).pos(centerX - 80, btnY).size(60, 20).build();

        resetBtn.active = canReset; // ✅ 오늘이면 비활성화
        this.addRenderableWidget(resetBtn);
//        // 다시받기 버튼
//        this.addRenderableWidget(Button.builder(Component.literal("다시받기"), btn -> {
//            // TODO: 다시받기 로직
//        }).pos(centerX - 80, btnY).size(60, 20).build());

        // 보상수령 버튼
        //boolean canClaimDaily = dailyMissions.stream().allMatch(m -> m.completed && !m.rewarded);
        boolean canClaimDaily = dailyMissions.stream().anyMatch(m -> m.completed && !m.rewarded);

        boolean canClaimWeekly = weeklyMissions.stream().anyMatch(m -> m.completed && !m.rewarded);

        Button claimBtn = Button.builder(Component.literal("보상수령"), btn -> {
            // 전체 일일 or 주간 보상 수령
            for (PlayerMissionData m : missions) {
                if (m.completed && !m.rewarded) {
                    tryClaim(m);
                }
            }
        }).pos(centerX, btnY).size(60, 20).build();
        claimBtn.active = canClaimDaily || canClaimWeekly;
        this.addRenderableWidget(claimBtn);

        // 닫기 버튼
        this.addRenderableWidget(Button.builder(Component.literal("닫기"), btn -> {
            Minecraft.getInstance().setScreen(null);
        }).pos(centerX + 80, btnY).size(60, 20).build());
    }

    private void tryClaim(PlayerMissionData mission) {
        if (mission.completed && !mission.rewarded) {
            AnsanPack.NETWORK.sendToServer(new MessageClaimReward(mission.missionId));
        }
    }

    public void markMissionRewarded(String missionId) {
        for (PlayerMissionData mission : missions) {
            if (mission.missionId.equals(missionId)) {
                mission.rewarded = true;
                break;
            }
        }
        this.init(); // UI 재갱신
    }
    public static void openFromInfo(List<MessageOpenMissionUI.MissionInfo> infoList, boolean canReset) {
        List<PlayerMissionData> missions = infoList.stream().map(info -> {
            PlayerMissionData data = new PlayerMissionData("", info.missionId, info.progress, info.completed, info.rewarded, null);
            data.type = info.type;
            data.description = info.description;
            data.goalValue = info.goalValue; // ✅ 목표 수치 클라이언트에 전달
            return data;
        }).toList();
        Minecraft.getInstance().setScreen(new MissionScreen(missions, canReset));
    }


    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        super.render(g, mx, my, pt);
        g.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}
