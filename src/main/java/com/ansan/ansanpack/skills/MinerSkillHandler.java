package com.ansan.ansanpack.skills;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;

public class MinerSkillHandler {

    public static void handleBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;

        if (!SkillUtils.PlayerHasSkillCategory(player, "puffish_skills:mining")) return;

        BlockState state = event.getState();
        Block block = state.getBlock();

        // ✅ 확정 스킬: Auto Smelt
        double autoSmelt = SkillUtils.getAttributeValue(player, ModAttributes.AUTO_SMELT_ORE.get());
        if (SkillUtils.isOreBlock(block) && autoSmelt >= 1.0D) {
            ItemStack smelted = getSmeltedResult(block);
            if (!smelted.isEmpty()) {
                block.popResource(player.level(), event.getPos(), smelted);
                AnsanPack.LOGGER.info("[SKILL] Miner - Auto smelted ore drop granted at {}", event.getPos());
            }
            return; // Auto Smelt 시 추가 Drop 스킵
        }

        // ✅ 확률 기반: Extra Ore Drop
        double extraOreDropChance = SkillUtils.getAttributeValue(player, ModAttributes.EXTRA_ORE_DROP_CHANCE.get());
        if (SkillUtils.isOreBlock(block) && player.level().random.nextFloat() < extraOreDropChance) {
            ItemStack smelted = getSmeltedResult(block);
            if (!smelted.isEmpty()) {
                block.popResource(player.level(), event.getPos(), smelted);
                AnsanPack.LOGGER.info("[SKILL] Miner - Extra ore drop granted at {} (chance: {})", event.getPos(), extraOreDropChance);
            }
        }
    }


    public static void handleBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!SkillUtils.PlayerHasSkillCategory(player, "puffish_skills:mining")) return;

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof PickaxeItem) {
            event.setNewSpeed(event.getOriginalSpeed() * 1.25F); // 곡괭이 채광 속도 25% 증가
            AnsanPack.LOGGER.info("[SKILL] Miner - BreakSpeed boosted with pickaxe: new speed = {}", event.getNewSpeed());
        }
    }

    // ✅ 광물 판정 → forge:ores 태그 사용
    private static boolean isOreBlock(Block block) {
        return block.defaultBlockState().is(BlockTags.create(new ResourceLocation("forge:ores")));
    }

    // ✅ 간단한 smelted 결과 (임시)
    private static ItemStack getSmeltedResult(Block block) {
        if (block == Blocks.IRON_ORE) return new ItemStack(Items.IRON_INGOT);
        if (block == Blocks.DEEPSLATE_IRON_ORE) return new ItemStack(Items.IRON_INGOT);
        if (block == Blocks.GOLD_ORE) return new ItemStack(Items.GOLD_INGOT);
        if (block == Blocks.DEEPSLATE_GOLD_ORE) return new ItemStack(Items.GOLD_INGOT);
        if (block == Blocks.COPPER_ORE) return new ItemStack(Items.COPPER_INGOT);
        if (block == Blocks.DEEPSLATE_COPPER_ORE) return new ItemStack(Items.COPPER_INGOT);
        // 필요 시 추가 가능
        return ItemStack.EMPTY;
    }
}
