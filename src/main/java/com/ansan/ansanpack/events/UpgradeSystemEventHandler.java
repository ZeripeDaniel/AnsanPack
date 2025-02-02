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
    private static final UUID UPGRADE_HELMET_UUID = UUID.fromString("a5a6a7a8-1234-5678-9abc-def012345678");
    private static final UUID UPGRADE_CHEST_UUID = UUID.fromString("b5b6b7b8-2345-6789-abcd-ef0123456789");
    private static final UUID UPGRADE_LEGGINGS_UUID = UUID.fromString("c5c6c7c8-3456-7890-abcd-0123456789ab");
    private static final UUID UPGRADE_BOOTS_UUID = UUID.fromString("d5d6d7d8-4567-8901-b4dc-234567890123");

    private static final UUID UPGRADE_ATSPD_UUID = UUID.fromString("c5c6c7c8-3456-7890-abcd-0123456789ab");

    @SubscribeEvent
    public static void onItemAttribute(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        UpgradeConfigManager.getConfig(stack.getItem()).ifPresent(config -> {
            int level = WeaponUpgradeSystem.getCurrentLevel(stack);
            if (level > 0) {
                config.effects.forEach((effect, value) -> {
                    applyEffect(
                            event,
                            stack,
                            effect,
                            value,
                            level,
                            event.getSlotType() // 슬롯 정보 전달
                    );
                });
            }
        });
    }
    private static void applyEffect(ItemAttributeModifierEvent event, ItemStack stack, String effect, double bonus, int level, EquipmentSlot slot) {
        double value = Math.round(bonus * level * 100) / 100.0; // 소수점 2자리 제한

        // ▼▼▼ 방어구 효과 처리 ▼▼▼
        if (stack.getItem() instanceof ArmorItem armor) {
            ArmorItem.Type armorType = armor.getType();
            switch (effect) {
                case "helmet_armor" -> {
                    if (armorType == ArmorItem.Type.HELMET && slot == EquipmentSlot.HEAD) {
                        addModifier(event, Attributes.ARMOR, UPGRADE_HELMET_UUID, "헬멧 방어력 강화", value);
                    }
                }
                case "chest_armor" -> {
                    if (armorType == ArmorItem.Type.CHESTPLATE && slot == EquipmentSlot.CHEST) {
                        addModifier(event, Attributes.ARMOR, UPGRADE_CHEST_UUID, "흉갑 방어력 강화", value);
                    }
                }
                case "leggings_armor" -> {
                    if (armorType == ArmorItem.Type.LEGGINGS && slot == EquipmentSlot.LEGS) {
                        addModifier(event, Attributes.ARMOR, UPGRADE_LEGGINGS_UUID, "레깅스 방어력 강화", value);
                    }
                }
                case "boots_armor" -> {
                    if (armorType == ArmorItem.Type.BOOTS && slot == EquipmentSlot.FEET) {
                        addModifier(event, Attributes.ARMOR, UPGRADE_BOOTS_UUID, "부츠 방어력 강화", value);
                    }
                }
            }
        }

        // ▼▼▼ 무기 효과 처리 ▼▼▼
        else if (stack.getItem() instanceof SwordItem) {
            if ((slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND)) {
                switch (effect) {
                    case "damage_per_level" ->
                            addModifier(event, Attributes.ATTACK_DAMAGE, UPGRADE_DAMAGE_UUID, "무기 공격력 강화", value);
                    case "attack_spd_level" ->
                            addModifier(event, Attributes.ATTACK_SPEED, UPGRADE_ATSPD_UUID, "공격 속도 강화", value);
                }
            }
        }

        // ▼▼▼ 디버깅 로그 ▼▼▼
        AnsanPack.LOGGER.debug("[효과 적용] {}: {}+{} (슬롯: {})",
                stack.getDisplayName().getString(),
                effect,
                value,
                slot.getName()
        );
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
        if (event.getEntity() == null) return; // 서버 측 실행 방지
        ItemStack stack = event.getItemStack();
        WeaponUpgradeSystem.addUpgradeTooltip(stack, event.getToolTip());
    }

}
