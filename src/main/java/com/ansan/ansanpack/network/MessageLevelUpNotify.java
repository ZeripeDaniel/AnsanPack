package com.ansan.ansanpack.network;

import com.ansan.ansanpack.sound.ModSoundEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageLevelUpNotify {

    public MessageLevelUpNotify() {
        // 내용 없음
    }

    public static MessageLevelUpNotify decode(FriendlyByteBuf buf) {
        return new MessageLevelUpNotify();
    }

    public void encode(FriendlyByteBuf buf) {
        // 내용 없음
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.level().playSound(
                    null, // null = 주변 모든 플레이어에게 들림
                    player.getX(), player.getY(), player.getZ(),
                    ModSoundEvents.LEVEL_UP.get(),
                    SoundSource.PLAYERS,
                    1.0f, 1.0f
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
