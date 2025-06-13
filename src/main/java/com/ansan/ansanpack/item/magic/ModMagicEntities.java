package com.ansan.ansanpack.item.magic;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.resources.ResourceLocation;

public class ModMagicEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AnsanPack.MODID);

    public static final RegistryObject<EntityType<MagicProjectileEntity>> MAGIC_PROJECTILE =
            ENTITIES.register("magic_projectile",
                    () -> EntityType.Builder.<MagicProjectileEntity>of(
                                    MagicProjectileEntity::new,
                                    MobCategory.MISC)
                            .sized(0.25f, 0.25f) // 크기: 작고 둥글게
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(new ResourceLocation(AnsanPack.MODID, "magic_projectile").toString()));
}
