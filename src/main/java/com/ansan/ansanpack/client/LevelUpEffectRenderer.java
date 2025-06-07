package com.ansan.ansanpack.client;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelUpEffectRenderer {

    private static final ResourceLocation LEVEL_UP_IMAGE = new ResourceLocation(AnsanPack.MODID, "textures/gui/level_up.png");

    private static final long DISPLAY_DURATION_MS = 3000; // 4초 지속
    private static long displayStartTime = -1;

    public static void trigger() {
        displayStartTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Post event) {
        if (displayStartTime < 0) return;

        long now = System.currentTimeMillis();
        long elapsed = now - displayStartTime;
        if (elapsed >= DISPLAY_DURATION_MS) {
            displayStartTime = -1;
            return;
        }

        // alpha = 1.0f → 0.0f 점진적 감소
        float alpha = 1.0f - (float) elapsed / DISPLAY_DURATION_MS;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics gui = event.getGuiGraphics();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int imageWidth = 256;
        int imageHeight = 128;

        int x = (screenWidth - imageWidth) / 2;
        int y = (screenHeight - imageHeight) / 2;

        gui.setColor(1.0f, 1.0f, 1.0f, alpha);
        gui.blit(LEVEL_UP_IMAGE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        gui.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
