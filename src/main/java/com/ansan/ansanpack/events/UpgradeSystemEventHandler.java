package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UpgradeSystemEventHandler {
    private static final UUID UPGRADE_DAMAGE_UUID = UUID.fromString("a5a6a7a8-1234-5678-9abc-def012345678");
    private static final UUID UPGRADE_ARMOR_UUID = UUID.fromString("b5b6b7b8-2345-6789-abcd-ef0123456789");
    private static final UUID UPGRADE_ATSPD_UUID = UUID.fromString("c5c6c7c8-3456-7890-abcd-0123456789ab");

    @SubscribeEvent
    public static void onItemAttribute(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        UpgradeConfigManager.getConfig(stack.getItem()).ifPresent(config -> {
            int level = WeaponUpgradeSystem.getCurrentLevel(stack);
            if (level > 0) {
                config.effects.forEach((effect, value) -> {
                    double bonus = value * level;
                    EquipmentSlot slot = event.getSlotType();

                    // 방어구 효과 처리
                    if (stack.getItem() instanceof ArmorItem armor) {
                        ArmorItem.Type armorType = armor.getType();
                        switch (effect) {
                            case "helmet_armor":
                                if (armorType == ArmorItem.Type.HELMET && slot == EquipmentSlot.HEAD) {
                                    addModifier(event, Attributes.ARMOR, UPGRADE_ARMOR_UUID, "Helmet Armor Bonus", bonus);
                                }
                                break;
                            case "chest_armor":
                                if (armorType == ArmorItem.Type.CHESTPLATE && slot == EquipmentSlot.CHEST) {
                                    addModifier(event, Attributes.ARMOR, UPGRADE_ARMOR_UUID, "Chest Armor Bonus", bonus);
                                }
                                break;
                            case "leggings_armor":
                                if (armorType == ArmorItem.Type.LEGGINGS && slot == EquipmentSlot.LEGS) {
                                    addModifier(event, Attributes.ARMOR, UPGRADE_ARMOR_UUID, "Leggings Armor Bonus", bonus);
                                }
                            break;
                            case "boots_armor":
                                if (armorType == ArmorItem.Type.BOOTS && slot == EquipmentSlot.FEET) {
                                    addModifier(event, Attributes.ARMOR, UPGRADE_ARMOR_UUID, "Boots Armor Bonus", bonus);
                                }
                            break;
                        }
                    }
                    // 무기 효과 처리
                    else if (stack.getItem() instanceof SwordItem && (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND)) {
                        if ("damage_per_level".equals(effect)) {
                            addModifier(event, Attributes.ATTACK_DAMAGE, UPGRADE_DAMAGE_UUID, "Weapon Damage Bonus", bonus);
                        }
                        if("attack_spd_level".equals(effect)) {
                            addModifier(event, Attributes.ATTACK_SPEED, UPGRADE_ATSPD_UUID, "Attack Speed Bonus", bonus);
                        }
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
        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        WeaponUpgradeSystem.addUpgradeTooltip(stack, event.getToolTip());
    }

}
