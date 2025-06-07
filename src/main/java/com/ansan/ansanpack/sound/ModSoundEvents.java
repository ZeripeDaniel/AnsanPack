package com.ansan.ansanpack.sound;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AnsanPack.MODID);
    public static final RegistryObject<SoundEvent> LEVEL_UP = SOUND_EVENTS.register("level_up",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(AnsanPack.MODID, "level/level_up")));

}
