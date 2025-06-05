package com.ansan.ansanpack.client;

import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

import static com.ansan.ansanpack.client.ModParticles.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UpgradeAuraRenderHandler {

    private static final Random random = new Random();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // STAGE.AFTER_SKY, AFTER_ENTITIES 등이 가능함
        if (event.getStage() != Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.level == null || mc.player == null) return;

        LocalPlayer player = mc.player;
        ItemStack held = player.getMainHandItem();
        int level = WeaponUpgradeSystem.getCurrentLevel(held);

        if (level < 5) return;

        double x = player.getX() + (random.nextDouble() - 0.5) * 0.6;
        double y = player.getY() + 1.0 + (random.nextDouble() * 0.5);
        double z = player.getZ() + (random.nextDouble() - 0.5) * 0.6;

        ParticleOptions particle = switch (level) {
            case 5, 6, 7, 8, 9       -> GLOW_YELLOW.get();
            case 10,11,12,13,14     -> GLOW_RED.get();
            case 15,16,17,18,19     -> GLOW_BLUE.get();
            default                 -> GLOW_GREEN.get();
        };

        mc.level.addParticle(particle, x, y, z, 0, 0.01, 0);
    }
}
