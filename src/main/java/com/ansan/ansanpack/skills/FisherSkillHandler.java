package com.ansan.ansanpack.skills;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.ItemFishedEvent;

public class FisherSkillHandler {

    public static void handleFishing(ItemFishedEvent event) {
        Player player = (Player) event.getEntity();
        if (player == null || player.level().isClientSide) return;

        if (!SkillUtils.PlayerHasSkillCategory(player, "puffish_skills:fishing")) return;

        // ✅ 확률 기반: Extra Fish Loot
        double extraFishChance = SkillUtils.getAttributeValue(player, ModAttributes.EXTRA_FISH_LOOT_CHANCE.get());
        if (player.level().random.nextFloat() < extraFishChance) {
            ItemStack extraFish = new ItemStack(Items.COD); // 예시로 COD
            event.getDrops().add(extraFish);

            AnsanPack.LOGGER.info("[SKILL] Fisherman - Extra fish drop granted (chance: {})", extraFishChance);
        }
    }
}
