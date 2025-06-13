package com.ansan.ansanpack.skills;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SkillUtils {

    public static boolean PlayerHasSkillCategory(LivingEntity entity, String category) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player)) return false;

        CompoundTag data = player.getPersistentData();
        String NBT_KEY = "ansanpack_jobs";

        if (data.contains(NBT_KEY)) {
            ListTag jobList = data.getList(NBT_KEY, net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < jobList.size(); i++) {
                String jobName = jobList.getString(i);
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
        return block.defaultBlockState().is(BlockTags.create(new ResourceLocation("forge:ores")));
    }

    // ✅ Player → LivingEntity 확장
    public static double getAttributeValue(LivingEntity entity, Attribute attribute) {
        if (entity == null || attribute == null) return 0.0;
        var instance = entity.getAttribute(attribute);
        return instance != null ? instance.getValue() : 0.0;
    }
}
