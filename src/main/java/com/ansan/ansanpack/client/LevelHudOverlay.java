package com.ansan.ansanpack.client;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.level.LocalCombatPowerData;
import com.ansan.ansanpack.client.level.LocalPlayerCardData;
import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import com.ansan.ansanpack.gui.StatScreen;
import com.ansan.ansanpack.network.MessageRequestMoneyOnly;
import com.ansan.ansanpack.network.MessageRequestSaveLevel;
import com.ansan.ansanpack.network.MessageRequestSaveStats;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LevelHudOverlay {

    enum HudState {
        OFF, LEFT, RIGHT;

        public HudState next() {
            return switch (this) {
                case OFF -> LEFT;
                case LEFT -> RIGHT;
                case RIGHT -> OFF;
            };
        }
    }

    private static HudState state = HudState.RIGHT;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindingRegistry.TOGGLE_CARD.consumeClick()) {
            state = state.next();
            String msg = switch (state) {
                case OFF -> "명함 UI 비활성화됨";
                case LEFT -> "명함 UI 좌측 정렬됨";
                case RIGHT -> "명함 UI 우측 정렬됨";
            };
            Minecraft.getInstance().player.displayClientMessage(Component.literal(msg), true);
            if (state != HudState.OFF) {
                AnsanPack.NETWORK.sendToServer(new MessageRequestMoneyOnly());

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

        if (KeyBindingRegistry.OPEN_STAT_WINDOW.consumeClick()) {
            StatScreen.open();
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (state == HudState.OFF) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.level == null) return;

        GuiGraphics gui = event.getGuiGraphics();
        LocalPlayer player = mc.player;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int x = (state == HudState.RIGHT) ? screenWidth - 130 : 10;
        int y = 10;

        gui.fill(x, y, x + 120, y + 60, 0x80000000);
        gui.blit(player.getSkinTextureLocation(), x + 4, y + 4, 8, 8, 8, 8, 64, 64);

        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.scale(0.75f, 0.75f, 0.75f);

        gui.drawString(mc.font, player.getName().getString(), (int) ((x + 24) / 0.75f), (int) ((y + 4) / 0.75f), 0xFFFFFF);
        gui.drawString(mc.font, "안공: " + LocalPlayerCardData.INSTANCE.getMoney(), (int) ((x + 24) / 0.75f), (int) ((y + 16) / 0.75f), 0xDDFF55);
        gui.drawString(mc.font, "LV. " + LocalPlayerLevelData.INSTANCE.getLevel(), (int) ((x + 24) / 0.75f), (int) ((y + 28) / 0.75f), 0x55FFFF);
        gui.drawString(mc.font, "전투력: " + String.format("%.2f", LocalCombatPowerData.get()), (int) ((x + 24) / 0.75f), (int) ((y + 40) / 0.75f), 0xFFAA55);

        pose.popPose();

        // 경험치 바
        double exp = LocalPlayerLevelData.INSTANCE.getExp();
        double maxExp = LocalPlayerLevelData.INSTANCE.getExpToNextLevel();
        double ratio = exp / maxExp;
        int barWidth = 90;
        int barHeight = 4;
        int barX = x + 20;
        int barY = y + 52;

        gui.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        gui.fill(barX, barY, barX + (int) (barWidth * ratio), barY + barHeight, 0xFF00FF00);

        // 경험치 수치 작게 표시
        PoseStack pose2 = gui.pose();
        pose2.pushPose();
        pose2.scale(0.65f, 0.65f, 0.65f);
        gui.drawString(mc.font, String.format("%.2f / %.2f", exp, maxExp),
                (int) (barX + (barX / 2) / 0.65f), (int) ((barY) / 0.65f), 0xAAAAAA);
        pose2.popPose();
    }

    private static int getScore(LocalPlayer player) {
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("ansan_money");
        if (obj != null && board.hasPlayerScore(player.getScoreboardName(), obj)) {
            return board.getOrCreatePlayerScore(player.getScoreboardName(), obj).getScore();
        }
        return 0;
    }
}
