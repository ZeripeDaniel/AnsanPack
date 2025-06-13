package com.ansan.ansanpack.client;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.MagicProjectileRenderer;
import com.ansan.ansanpack.item.magic.ModMagicEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModMagicEntities.MAGIC_PROJECTILE.get(), MagicProjectileRenderer::new);
    }
}
