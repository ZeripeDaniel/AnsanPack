package com.ansan.ansanpack.events;

import com.ansan.ansanpack.network.MessageUpgradeResult;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

public class SoundEventHandler {
    public static void handleUpgradeSound(MessageUpgradeResult msg, NetworkEvent.Context ctx) {
        Minecraft.getInstance().execute(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                var sound = msg.success
                        ? SoundEvents.FIREWORK_ROCKET_TWINKLE
                        : SoundEvents.PIG_HURT;

                player.level().playSound(
                        player,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        sound,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );
            }
        });
    }
}
