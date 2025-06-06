package com.ansan.ansanpack.gui;

import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 스탯 분배 화면 (= 키로 열림)
 */
public class StatScreen extends Screen {

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

        this.addRenderableWidget(Button.builder(
                Component.literal("힘: " + data.getStat("str") + " [+]"),
                btn -> data.gainPoint("str")
        ).pos(centerX - 60, startY).size(120, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("민첩: " + data.getStat("agi") + " [+]"),
                btn -> data.gainPoint("agi")
        ).pos(centerX - 60, startY + 25).size(120, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("지능: " + data.getStat("int") + " [+]"),
                btn -> data.gainPoint("int")
        ).pos(centerX - 60, startY + 50).size(120, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("행운: " + data.getStat("luck") + " [+]"),
                btn -> data.gainPoint("luck")
        ).pos(centerX - 60, startY + 75).size(120, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("닫기"),
                btn -> this.onClose()
        ).pos(centerX - 30, startY + 110).size(60, 20).build());
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
}
