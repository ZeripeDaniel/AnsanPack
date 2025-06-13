package com.ansan.ansanpack.util;

import com.ansan.ansanpack.skills.ModAttributes;
import com.ansan.ansanpack.skills.SkillUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientHelper {

    @OnlyIn(Dist.CLIENT)
    public static double getClientPlayerMagicAttack() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            return SkillUtils.getAttributeValue(player, ModAttributes.MAGIC_ATTACK.get());
        }
        return 0.0;
    }
}
