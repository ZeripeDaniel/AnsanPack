package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.network.MessageGainExp;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FishingExpHandler {

    @SubscribeEvent
    public static void onFishCaught(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isCanceled()) return;
        if (player.level().isClientSide) return;

        double gain = 1.0; // 낚시 성공 시 부여할 경험치
        AnsanPack.NETWORK.sendTo(
                new MessageGainExp(gain),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }
}
