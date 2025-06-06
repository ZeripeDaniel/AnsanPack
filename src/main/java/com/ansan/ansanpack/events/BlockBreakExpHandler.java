package com.ansan.ansanpack.events;

import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.ToolActions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Tiers;
import com.ansan.ansanpack.network.MessageGainExp;
import com.ansan.ansanpack.AnsanPack;

@Mod.EventBusSubscriber(modid = "ansanpack", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockBreakExpHandler {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        BlockState state = event.getState();
        Block block = state.getBlock();

        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof TieredItem)) return;
        if (!state.requiresCorrectToolForDrops()) return;
        if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.OAK_LEAVES) return;

        int gain = 2;

        // 서버 → 클라이언트로 경험치 부여 패킷 전송
        AnsanPack.NETWORK.sendTo(new MessageGainExp(gain), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
    }

}
