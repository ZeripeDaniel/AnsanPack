package com.ansan.ansanpack.client;

import com.ansan.ansanpack.item.magic.MagicProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class MagicProjectileRenderer extends ThrownItemRenderer<MagicProjectileEntity> {

    public MagicProjectileRenderer(EntityRendererProvider.Context context) {
        super(context); // scale (기본 1.0f 추천)
    }
}
