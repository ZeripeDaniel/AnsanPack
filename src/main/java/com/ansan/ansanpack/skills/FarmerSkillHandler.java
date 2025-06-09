package com.ansan.ansanpack.skills;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.skills.SkillUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;

public class FarmerSkillHandler {

    public static void handleBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;

        if (!SkillUtils.PlayerHasSkillCategory(player, "puffish_skills:farming")) return;

        BlockState state = event.getState();
        Block block = state.getBlock();

        // ✅ 농작물 최대 성장 상태일 때 추가 드랍
        if (block instanceof CropBlock crop && crop.isMaxAge(state)) {
            double dropChance = SkillUtils.getAttributeValue(player, ModAttributes.EXTRA_CROP_DROP_CHANCE.get());
            if (player.level().random.nextFloat() < dropChance) {
                ItemStack bonus = new ItemStack(block.asItem());
                block.popResource(player.level(), event.getPos(), bonus);

                AnsanPack.LOGGER.info("[SKILL] Farmer - Extra crop drop granted at {} (chance: {})", event.getPos(), dropChance);
            }
        }

        // ✅ 내구도 감소율 적용
        ItemStack tool = player.getMainHandItem();
        if (tool.getItem() instanceof HoeItem || tool.getItem() instanceof ShovelItem) {
            double reduceFactor = SkillUtils.getAttributeValue(player, ModAttributes.TOOL_DURABILITY_REDUCE_FACTOR.get());
            int currentDamage = tool.getDamageValue();
            int reducedDamage = (int) (currentDamage * reduceFactor);

            if (reducedDamage < currentDamage) {
                tool.setDamageValue(reducedDamage);
                AnsanPack.LOGGER.info("[SKILL] Farmer - Tool durability reduced ({} → {})", currentDamage, reducedDamage);
            }
        }
    }
}
