package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.network.MessageGainExp;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CropHarvestExpHandler {

    @SubscribeEvent
    public static void onCropBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        BlockState state = event.getState();
        Block block = state.getBlock();

        // ✅ CropBlock이고 최대 성장 상태일 때만 경험치 지급
        if (block instanceof CropBlock crop && crop.isMaxAge(state)) {
            AnsanPack.NETWORK.sendTo(
                    new MessageGainExp(0.33),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }
    }
}
