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
}
