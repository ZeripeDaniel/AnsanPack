package com.ansan.ansanpack.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClientParticles {

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.GLOW_YELLOW.get(), GlowAuraParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GLOW_RED.get(), GlowAuraParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GLOW_BLUE.get(), GlowAuraParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GLOW_GREEN.get(), GlowAuraParticle.Provider::new);
    }
}
