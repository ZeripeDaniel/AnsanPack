package com.ansan.ansanpack.client;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.KeyMapping;

import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
class LevelHudOverlay {

    private static boolean enabled = true;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindingRegistry.TOGGLE_CARD.consumeClick()) {
            enabled = !enabled;
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("명함 UI " + (enabled ? "활성화됨" : "비활성화됨")), true
            );
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.level == null) return;

        GuiGraphics gui = event.getGuiGraphics();
        LocalPlayer player = mc.player;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int x = screenWidth - 130;
        int y = 10;

        gui.fill(x, y, x + 120, y + 40, 0x80000000);
        gui.blit(player.getSkinTextureLocation(), x + 4, y + 4, 8, 8, 8, 8, 64, 64);
        gui.drawString(mc.font, player.getName(), x + 24, y + 4, 0xFFFFFF);
        gui.drawString(mc.font, "안산머니: " + getScore(player), x + 24, y + 16, 0xDDFF55);
        gui.drawString(mc.font, "LV. " + LocalPlayerLevelData.INSTANCE.getLevel(), x + 24, y + 28, 0x55FFFF);
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
