package com.ansan.ansanpack.skills;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SkillUtils {

    public static boolean PlayerHasSkillCategory(Player player, String category) {
        CompoundTag data = player.getPersistentData();
        String NBT_KEY = "ansanpack_jobs";

        if (data.contains(NBT_KEY)) {
            ListTag jobList = data.getList(NBT_KEY, net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < jobList.size(); i++) {
                String jobName = jobList.getString(i);

                // 직업 이름과 category 비교 (대소문자 구분 없음)
                if (jobName.equalsIgnoreCase(category)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isCropBlock(Block block) {
        return block instanceof CropBlock;
    }

    public static boolean isCropFullyGrown(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        return false;
    }

    public static boolean isOreBlock(Block block) {
        // ✅ forge:ores 태그 기반 체크 → 모든 광물 블록 포함
        return block.defaultBlockState().is(BlockTags.create(new ResourceLocation("forge:ores")));
    }
    public static double getAttributeValue(Player player, Attribute attribute) {
        if (player.getAttributes().hasAttribute(attribute)) {
            return player.getAttributeValue(attribute);
        }
        return 0.0D;
    }

}
