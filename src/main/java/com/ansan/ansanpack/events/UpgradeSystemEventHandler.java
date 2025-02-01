package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UpgradeSystemEventHandler {
    private static final UUID UPGRADE_DAMAGE_UUID = UUID.fromString("a5a6a7a8-1234-5678-9abc-def012345678");
    private static final UUID UPGRADE_ARMOR_UUID = UUID.fromString("b5b6b7b8-2345-6789-abcd-ef0123456789");
    private static final UUID UPGRADE_ARMOR_TOUGHNESS_UUID = UUID.fromString("c5c6c7c8-3456-7890-bcde-f01234567890");
    private static final UUID UPGRADE_ATTACK_SPEED_UUID = UUID.fromString("d5d6d7d8-4567-8901-cdef-012345678901");

    @SubscribeEvent
    public static void onItemAttribute(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        UpgradeConfigManager.getConfig(stack.getItem()).ifPresent(config -> {
            int level = WeaponUpgradeSystem.getCurrentLevel(stack);
            if (level > 0) {
                config.effects.forEach((effect, value) -> {
                    double bonus = value * level;
                    switch (effect) {
                        case "damage_per_level":
                            addModifier(event, Attributes.ATTACK_DAMAGE, UPGRADE_DAMAGE_UUID, "Upgrade Damage Bonus", bonus);
                            break;
                        case "armor_per_level":
                            addModifier(event, Attributes.ARMOR, UPGRADE_ARMOR_UUID, "Upgrade Armor Bonus", bonus);
                            break;
                        case "armor_toughness_per_level":
                            addModifier(event, Attributes.ARMOR_TOUGHNESS, UPGRADE_ARMOR_TOUGHNESS_UUID, "Upgrade Armor Toughness Bonus", bonus);
                            break;
                        case "attack_speed_per_level":
                            addModifier(event, Attributes.ATTACK_SPEED, UPGRADE_ATTACK_SPEED_UUID, "Upgrade Attack Speed Bonus", bonus);
                            break;
                        // 추가 효과 타입은 여기에 case를 추가하면 됩니다.
                    }
                });
            }
        });
    }

    private static void addModifier(ItemAttributeModifierEvent event, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID uuid, String name, double amount) {
        event.addModifier(
                attribute,
                new AttributeModifier(
                        uuid,
                        name,
                        amount,
                        AttributeModifier.Operation.ADDITION
                )
        );
    }
}
