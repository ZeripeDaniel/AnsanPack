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

import java.util.*;
import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UpgradeSystemEventHandler {
    // UUID는 고유해야 하므로 모든 속성 별로 다르게 생성
    private static final UUID UPGRADE_DAMAGE_UUID = UUID.fromString("11111111-aaaa-bbbb-cccc-111111111111");
    private static final UUID UPGRADE_ATSPD_UUID = UUID.fromString("22222222-aaaa-bbbb-cccc-222222222222");

    private static final UUID UPGRADE_HELMET_UUID = UUID.fromString("33333333-aaaa-bbbb-cccc-333333333333");
    private static final UUID UPGRADE_CHEST_UUID = UUID.fromString("44444444-aaaa-bbbb-cccc-444444444444");
    private static final UUID UPGRADE_LEGGINGS_UUID = UUID.fromString("55555555-aaaa-bbbb-cccc-555555555555");
    private static final UUID UPGRADE_BOOTS_UUID = UUID.fromString("66666666-aaaa-bbbb-cccc-666666666666");

    private static final UUID UPGRADE_HEALTH_UUID = UUID.fromString("77777777-aaaa-bbbb-cccc-777777777777");
    private static final UUID UPGRADE_KBRES_UUID = UUID.fromString("88888888-aaaa-bbbb-cccc-888888888888");
    private static final UUID UPGRADE_TOUGH_UUID = UUID.fromString("99999999-aaaa-bbbb-cccc-999999999999");
    private static final UUID UPGRADE_SPEED_UUID = UUID.fromString("aaaaaaaa-aaaa-bbbb-cccc-aaaaaaaaaaaa");
    private static final UUID UPGRADE_LUCK_UUID = UUID.fromString("bbbbbbbb-aaaa-bbbb-cccc-bbbbbbbbbbbb");
    private static final UUID UPGRADE_KNOCKBACK_UUID    = UUID.fromString("cccccccc-aaaa-bbbb-cccc-cccccccccccc");


    @SubscribeEvent
    public static void onItemAttribute(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        UpgradeConfigManager.getConfig(stack.getItem()).ifPresent(config -> {
            int level = WeaponUpgradeSystem.getCurrentLevel(stack);
            if (level <= 0) return;

            Map<String, Double> totalEffectValues = new HashMap<>();

            config.effects.forEach((effectKey, effectList) -> {
                for (UpgradeConfigManager.EffectEntry entry : effectList) {
                    if (level >= entry.applyLevel) {
                        double total = 0.0;

                        // applyLevel부터 level까지 착각한 레벨(fakeLevel)을 기준으로 multiplier 적용
                        for (int lv = entry.applyLevel; lv <= level; lv++) {
                            int fakeLevel = lv - entry.applyLevel + 1; // lv=applyLevel → 1, lv=applyLevel+1 → 2 ...
                            total += entry.value * WeaponUpgradeSystem.getEffectMultiplier(fakeLevel);
                        }

                        totalEffectValues.merge(effectKey, total, Double::sum);
                    }
                }
            });


            totalEffectValues.forEach((effect, totalValue) -> {
                applyEffect(event, stack, effect, totalValue, event.getSlotType());
            });
        });
    }


    private static void applyEffect(ItemAttributeModifierEvent event, ItemStack stack, String effect, double value, EquipmentSlot slot) {
        boolean isArmor = stack.getItem() instanceof ArmorItem armor;
        boolean isMainOrOffHand = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;

        // 방어구 전용 효과
        if (isArmor) {
            ArmorItem.Type type = ((ArmorItem) stack.getItem()).getType();
            switch (effect) {
                case "helmet_armor" -> {
                    if (type == ArmorItem.Type.HELMET && slot == EquipmentSlot.HEAD)
                        addModifier(event, Attributes.ARMOR, UPGRADE_HELMET_UUID, "헬멧 방어력 강화", value);
                }
                case "chest_armor" -> {
                    if (type == ArmorItem.Type.CHESTPLATE && slot == EquipmentSlot.CHEST)
                        addModifier(event, Attributes.ARMOR, UPGRADE_CHEST_UUID, "흉갑 방어력 강화", value);
                }
                case "leggings_armor" -> {
                    if (type == ArmorItem.Type.LEGGINGS && slot == EquipmentSlot.LEGS)
                        addModifier(event, Attributes.ARMOR, UPGRADE_LEGGINGS_UUID, "레깅스 방어력 강화", value);
                }
                case "boots_armor" -> {
                    if (type == ArmorItem.Type.BOOTS && slot == EquipmentSlot.FEET)
                        addModifier(event, Attributes.ARMOR, UPGRADE_BOOTS_UUID, "부츠 방어력 강화", value);
                }
            }
        }

        // 무기 전용 효과
        if (isMainOrOffHand) {
            switch (effect) {
                case "damage_per_level" ->
                        addModifier(event, Attributes.ATTACK_DAMAGE, UPGRADE_DAMAGE_UUID, "무기 공격력 강화", value);
                case "attack_spd_level" ->
                        addModifier(event, Attributes.ATTACK_SPEED, UPGRADE_ATSPD_UUID, "공격 속도 강화", value);
                case "knockback_level" ->
                        addModifier(event, Attributes.ATTACK_KNOCKBACK, UPGRADE_KNOCKBACK_UUID, "공격시 넉백 강화", value);
            }
        }

        // 공통 효과
        switch (effect) {
            case "health_bonus" ->
                    addModifier(event, Attributes.MAX_HEALTH, UPGRADE_HEALTH_UUID, "체력 증가", value);
            case "resist_knockback" ->
                    addModifier(event, Attributes.KNOCKBACK_RESISTANCE, UPGRADE_KBRES_UUID, "넉백 저항", value);
            case "toughness_bonus" ->
                    addModifier(event, Attributes.ARMOR_TOUGHNESS, UPGRADE_TOUGH_UUID, "방어 강인함", value);
            case "move_speed_bonus" ->
                    addModifier(event, Attributes.MOVEMENT_SPEED, UPGRADE_SPEED_UUID, "이동 속도 증가", value);
            case "luck_bonus" ->
                    addModifier(event, Attributes.LUCK, UPGRADE_LUCK_UUID, "행운", value);
        }

        AnsanPack.LOGGER.debug("[효과 적용] {}: {} +{} (슬롯: {})",
                stack.getDisplayName().getString(), effect, value, slot.getName());
    }



    private static void addModifier(ItemAttributeModifierEvent event, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID uuid, String name, double amount) {
        event.addModifier(attribute, new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.ADDITION));
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) return;
        WeaponUpgradeSystem.addUpgradeTooltip(event.getItemStack(), event.getToolTip());
    }
}
