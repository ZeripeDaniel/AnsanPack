package com.ansan.ansanpack.client;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "ansanpack");

    public static final RegistryObject<SimpleParticleType> GLOW_YELLOW = PARTICLES.register("glow_yellow", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> GLOW_RED    = PARTICLES.register("glow_red", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> GLOW_BLUE   = PARTICLES.register("glow_blue", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> GLOW_GREEN  = PARTICLES.register("glow_green", () -> new SimpleParticleType(true));
}
