package com.ansan.ansanpack.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.Map;

public class ModArmorMaterial implements ArmorMaterial {
    private static final Map<Type, Integer> DURABILITY = new EnumMap<>(Map.of(
            Type.HELMET, 13,
            Type.CHESTPLATE, 15,
            Type.LEGGINGS, 16,
            Type.BOOTS, 11
    ));

    private static final Map<Type, Integer> DEFENSE = new EnumMap<>(Map.of(
            Type.HELMET, 4,
            Type.CHESTPLATE, 10,
            Type.LEGGINGS, 8,
            Type.BOOTS, 6
    ));

    private static final int DURABILITY_MULTIPLIER = 30;

    @Override
    public int getDurabilityForType(Type type) {
        return DURABILITY.getOrDefault(type, 0) * DURABILITY_MULTIPLIER;
    }

    @Override
    public int getDefenseForType(Type type) {
        return DEFENSE.getOrDefault(type, 0);
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_NETHERITE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.OBSIDIAN);
        // or later → Ingredient.of(ModItems.OBSIDIAN_INGOT.get());
    }
    @Override
    public String getName() {
        return "obsidian";
    }
    @Override
    public float getToughness() {
        return 4.0f;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.3f;
    }
}
