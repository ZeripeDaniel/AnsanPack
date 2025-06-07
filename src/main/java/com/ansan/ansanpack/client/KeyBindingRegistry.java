package com.ansan.ansanpack.client;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.gui.StatScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindingRegistry {
    public static final KeyMapping TOGGLE_CARD = new KeyMapping(
            "key.ansanpack.toggle_card", GLFW.GLFW_KEY_MINUS, "key.categories.misc"
    );
    public static final KeyMapping OPEN_STAT_WINDOW = new KeyMapping(
            "key.ansanpack.open_stat_window", GLFW.GLFW_KEY_EQUAL, "key.categories.misc"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_CARD);
        event.register(OPEN_STAT_WINDOW);
    }
}
