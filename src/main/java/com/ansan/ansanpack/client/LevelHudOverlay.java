package com.ansan.ansanpack.client;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.level.LocalPlayerCardData;
import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import com.ansan.ansanpack.gui.StatScreen;
import com.ansan.ansanpack.network.MessageRequestMoneyOnly;
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

    // HUD 위치 상태 열거형 정의
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

        // 배경 박스: 120x60
        gui.fill(x, y, x + 120, y + 60, 0x80000000);

        // 플레이어 정보
        gui.blit(player.getSkinTextureLocation(), x + 4, y + 4, 8, 8, 8, 8, 64, 64);
        gui.drawString(mc.font, player.getName(), x + 24, y + 4, 0xFFFFFF);
        gui.drawString(mc.font, "안공: " + LocalPlayerCardData.INSTANCE.getMoney(), x + 24, y + 16, 0xDDFF55);
        gui.drawString(mc.font, "LV. " + LocalPlayerLevelData.INSTANCE.getLevel(), x + 24, y + 28, 0x55FFFF);

        // 경험치 시각적 표현
        double exp = LocalPlayerLevelData.INSTANCE.getExp();
        double maxExp = 100 + (LocalPlayerLevelData.INSTANCE.getLevel() * 20);
        double ratio = exp / maxExp;
        int barWidth = 90;
        int barHeight = 6;
        int barX = x + 20;
        int barY = y + 45;

        // 바 배경
        gui.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        // 바 채움
        gui.fill(barX, barY, barX + (int) (barWidth * ratio), barY + barHeight, 0xFF00FF00);
        // 수치 표시
        gui.drawString(mc.font, String.format("%.0f / %.0f", exp, maxExp), barX, barY + 8, 0xAAAAAA);
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
