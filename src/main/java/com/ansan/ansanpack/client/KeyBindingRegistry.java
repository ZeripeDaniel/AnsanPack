package com.ansan.ansanpack.client;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindingRegistry {
    public static final KeyMapping TOGGLE_CARD = new KeyMapping(
            "key.ansanpack.toggle_card", GLFW.GLFW_KEY_MINUS, "key.categories.misc"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_CARD);
    }
}
