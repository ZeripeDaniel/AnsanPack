package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.network.MessageGainExp;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = "ansanpack", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CropHarvestExpHandler {

    @SubscribeEvent
    public static void onCropRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        BlockPos pos = event.getPos();
        BlockState state = player.level().getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof CropBlock crop) {
            if (crop.isMaxAge(state)) {
                AnsanPack.NETWORK.sendTo(new MessageGainExp(2), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }
}
