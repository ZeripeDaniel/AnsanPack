package com.ansan.ansanpack.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
public class GlowAuraParticle extends TextureSheetParticle {
    protected GlowAuraParticle(ClientLevel level, double x, double y, double z,
                               double dx, double dy, double dz, SpriteSet spriteSet) {
        super(level, x, y, z, dx, dy, dz);
        this.setSprite(spriteSet.get(level.getRandom())); // ✔️ 실질적으로 안전하고 추천
        this.gravity = 0;
        this.lifetime = 40;
        this.scale(1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        // 천천히 위로
        this.yd += 0.002;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new GlowAuraParticle(level, x, y, z, dx, dy, dz, sprite);
        }
    }
}
