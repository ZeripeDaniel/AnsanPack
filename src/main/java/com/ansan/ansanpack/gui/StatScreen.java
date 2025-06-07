package com.ansan.ansanpack.gui;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import com.ansan.ansanpack.network.MessageRequestSaveLevel;
import com.ansan.ansanpack.network.MessageRequestSaveStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 스탯 분배 화면 (= 키로 열림)
 */
public class StatScreen extends Screen {

    private Button strBtn, agiBtn, intBtn, luckBtn;

    public StatScreen() {
        super(Component.literal("스탯 분배"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new StatScreen());
    }

    @Override
    protected void init() {
        this.clearWidgets();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;

        LocalPlayerStatData data = LocalPlayerStatData.INSTANCE;

        // 힘 버튼
        strBtn = Button.builder(
                Component.literal("힘: " + data.getStat("str") + " [+]"),
                btn -> {
                    data.gainPoint("str");
                    updateButtonTexts(); // 👈 텍스트 갱신
                }
        ).pos(centerX - 60, startY).size(120, 20).build();
        this.addRenderableWidget(strBtn);

        // 민첩 버튼
        agiBtn = Button.builder(
                Component.literal("민첩: " + data.getStat("agi") + " [+]"),
                btn -> {
                    data.gainPoint("agi");
                    updateButtonTexts();
                }
        ).pos(centerX - 60, startY + 25).size(120, 20).build();
        this.addRenderableWidget(agiBtn);

        // 지능 버튼
        intBtn = Button.builder(
                Component.literal("지능: " + data.getStat("int") + " [+]"),
                btn -> {
                    data.gainPoint("int");
                    updateButtonTexts();
                }
        ).pos(centerX - 60, startY + 50).size(120, 20).build();
        this.addRenderableWidget(intBtn);

        // 행운 버튼
        luckBtn = Button.builder(
                Component.literal("행운: " + data.getStat("luck") + " [+]"),
                btn -> {
                    data.gainPoint("luck");
                    updateButtonTexts();
                }
        ).pos(centerX - 60, startY + 75).size(120, 20).build();
        this.addRenderableWidget(luckBtn);

        // 닫기 버튼
        this.addRenderableWidget(Button.builder(
                Component.literal("닫기"),
                btn -> this.onClose()
        ).pos(centerX - 30, startY + 110).size(60, 20).build());
    }

    private void updateButtonTexts() {
        LocalPlayerStatData data = LocalPlayerStatData.INSTANCE;

        strBtn.setMessage(Component.literal("힘: " + data.getStat("str") + " [+]"));
        agiBtn.setMessage(Component.literal("민첩: " + data.getStat("agi") + " [+]"));
        intBtn.setMessage(Component.literal("지능: " + data.getStat("int") + " [+]"));
        luckBtn.setMessage(Component.literal("행운: " + data.getStat("luck") + " [+]"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int centerX = this.width / 2;
        int topY = this.height / 2 - 80;

        Minecraft mc = Minecraft.getInstance();
        String playerName = mc.player.getName().getString();
        int ap = LocalPlayerStatData.INSTANCE.getAvailableAP();

        graphics.drawCenteredString(this.font, "플레이어: " + playerName, centerX, topY, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "남은 AP: " + ap, centerX, topY + 12, 0xFFFF00);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        super.onClose();


        int level = LocalPlayerLevelData.INSTANCE.getLevel();
        double exp = LocalPlayerLevelData.INSTANCE.getExp();

        AnsanPack.NETWORK.sendToServer(new MessageRequestSaveLevel(level, exp));
        AnsanPack.NETWORK.sendToServer(new MessageRequestSaveStats(
                LocalPlayerStatData.INSTANCE.getStat("str"),
                LocalPlayerStatData.INSTANCE.getStat("agi"),
                LocalPlayerStatData.INSTANCE.getStat("int"),
                LocalPlayerStatData.INSTANCE.getStat("luck"),
                LocalPlayerStatData.INSTANCE.getAvailableAP()
        ));
    }
}
