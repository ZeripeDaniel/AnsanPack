package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import com.ansan.ansanpack.config.EntityConfigManager;
import java.util.Map;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityAttributeModifier {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        for (Map.Entry<String, EntityConfigManager.EntityAttributes> entry : EntityConfigManager.getAllEntityAttributes().entrySet()) {
            String entityId = entry.getKey();
            EntityConfigManager.EntityAttributes attributes = entry.getValue();

            ResourceLocation entityResourceLocation = new ResourceLocation(entityId);
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityResourceLocation);

            if (entityType != null) {
                event.add((EntityType<? extends LivingEntity>) entityType, Attributes.MAX_HEALTH, attributes.maxHealth);
                event.add((EntityType<? extends LivingEntity>) entityType, Attributes.MOVEMENT_SPEED, attributes.movementSpeed);
                event.add((EntityType<? extends LivingEntity>) entityType, Attributes.ARMOR, attributes.armor);
                event.add((EntityType<? extends LivingEntity>) entityType, Attributes.ATTACK_DAMAGE, attributes.attackDamage);
            }
        }
    }
}