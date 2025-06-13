package com.ansan.ansanpack.skills;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, AnsanPack.MODID);

    public static final RegistryObject<Attribute> AUTO_SMELT_ORE =
            ATTRIBUTES.register("auto_smelt_ore", () -> new RangedAttribute("attribute.ansanpack.auto_smelt_ore", 0.0D, 0.0D, 1.0D).setSyncable(true));

    public static final RegistryObject<Attribute> EXTRA_ORE_DROP_CHANCE =
            ATTRIBUTES.register("extra_ore_drop_chance", () -> new RangedAttribute("attribute.ansanpack.extra_ore_drop_chance", 0.0D, 0.0D, 10.0D).setSyncable(true));

    public static final RegistryObject<Attribute> EXTRA_CROP_DROP_CHANCE =
            ATTRIBUTES.register("extra_crop_drop_chance", () -> new RangedAttribute("attribute.ansanpack.extra_crop_drop_chance", 0.0D, 0.0D, 10.0D).setSyncable(true));

    public static final RegistryObject<Attribute> EXTRA_FISH_LOOT_CHANCE =
            ATTRIBUTES.register("extra_fish_loot_chance", () -> new RangedAttribute("attribute.ansanpack.extra_fish_loot_chance", 0.0D, 0.0D, 10.0D).setSyncable(true));

    public static final RegistryObject<Attribute> TOOL_DURABILITY_REDUCE_FACTOR =
            ATTRIBUTES.register("tool_durability_reduce_factor", () -> new RangedAttribute("attribute.ansanpack.tool_durability_reduce_factor", 1.0D, 0.0D, 1.0D).setSyncable(true));
    public static final RegistryObject<Attribute> LIFESTEAL_CHANCE =
            ATTRIBUTES.register("lifedrain_chance", () -> new RangedAttribute("attribute.ansanpack.lifesteal_chance", 0.0D, 0.0D, 1.0D).setSyncable(true));

    public static final RegistryObject<Attribute> LIFESTEAL_RATIO =
            ATTRIBUTES.register("lifedrain_ratio", () -> new RangedAttribute("attribute.ansanpack.lifesteal_ratio", 0.0D, 0.0D, 1.0D).setSyncable(true));

    public static final RegistryObject<Attribute> LIFESTEAL_MAX_AMOUNT =
            ATTRIBUTES.register("lifedrain_max_amount", () -> new RangedAttribute("attribute.ansanpack.lifesteal_max_amount", 0.0D, 0.0D, 20.0D).setSyncable(true));
    public static final RegistryObject<Attribute> CRITICAL_CHANCE =
            ATTRIBUTES.register("critical_chance", () -> new RangedAttribute("attribute.ansanpack.critical_chance", 0.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> CRITICAL_DAMAGE =
            ATTRIBUTES.register("critical_damage", () -> new RangedAttribute("attribute.ansanpack.critical_damage", 1.5D, 1.0D, 3.0D).setSyncable(true)); // 기본 1.5배, 최대 3.0배
    public static final RegistryObject<Attribute> MAGIC_ATTACK =
            ATTRIBUTES.register("magic_attack", () -> new RangedAttribute("attribute.ansanpack.magic_attack", 0.0D, 0.0D, 2048.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MAGIC_DEFENSE_FLAT =
            ATTRIBUTES.register("magic_defense_flat", () -> new RangedAttribute("attribute.ansanpack.magic_defense_flat", 0.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MAGIC_DEFENSE_PERCENT =
            ATTRIBUTES.register("magic_defense_percent", () -> new RangedAttribute("attribute.ansanpack.magic_defense_percent", 0.0D, 0.0D, 1.0D).setSyncable(true));
    public static final RegistryObject<Attribute> MAGIC_PROJECTILE_SPEED =
            ATTRIBUTES.register("magic_projectile_speed", () ->
                    new RangedAttribute("attribute.name.ansanpack.magic_projectile_speed", 1.0D, 0.1D, 10.0D).setSyncable(true));



}
