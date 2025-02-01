package com.ansan.ansanpack.events;

import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = com.ansan.ansanpack.AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UpgradeSystemEventHandler {
    private static final UUID UPGRADE_DAMAGE_UUID = UUID.fromString("a5a6a7a8-1234-5678-9abc-def012345678");

    @SubscribeEvent
    public static void onItemAttribute(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (WeaponUpgradeSystem.getUpgradeLevel(stack) > 0) {
            float multiplier = WeaponUpgradeSystem.getDamageMultiplier(stack);
            event.addModifier(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                            UPGRADE_DAMAGE_UUID,
                            "Upgrade Damage Multiplier",
                            multiplier - 1.0f,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    )
            );
        }
    }
}
