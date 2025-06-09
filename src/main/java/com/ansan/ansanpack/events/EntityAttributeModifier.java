package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.skills.ModAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import com.ansan.ansanpack.config.EntityConfigManager;
import java.util.Map;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID)
public class EntityAttributeModifier {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        // 기존 코드 유지 (네 커스텀 엔티티 Attribute 적용)
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

        // === 여기서 PlayerEntity 에 커스텀 Attribute 등록 ===
        event.add(EntityType.PLAYER, ModAttributes.AUTO_SMELT_ORE.get());
        event.add(EntityType.PLAYER, ModAttributes.EXTRA_ORE_DROP_CHANCE.get());
        event.add(EntityType.PLAYER, ModAttributes.EXTRA_CROP_DROP_CHANCE.get());
        event.add(EntityType.PLAYER, ModAttributes.EXTRA_FISH_LOOT_CHANCE.get());
        event.add(EntityType.PLAYER, ModAttributes.TOOL_DURABILITY_REDUCE_FACTOR.get());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        copyAttribute(oldPlayer, newPlayer, ModAttributes.AUTO_SMELT_ORE.get());
        copyAttribute(oldPlayer, newPlayer, ModAttributes.EXTRA_ORE_DROP_CHANCE.get());
        copyAttribute(oldPlayer, newPlayer, ModAttributes.EXTRA_CROP_DROP_CHANCE.get());
        copyAttribute(oldPlayer, newPlayer, ModAttributes.EXTRA_FISH_LOOT_CHANCE.get());
        copyAttribute(oldPlayer, newPlayer, ModAttributes.TOOL_DURABILITY_REDUCE_FACTOR.get());
    }
    private static void copyAttribute(Player oldPlayer, Player newPlayer, net.minecraft.world.entity.ai.attributes.Attribute attribute) {
        AttributeInstance oldInstance = oldPlayer.getAttributes().getInstance(attribute);
        AttributeInstance newInstance = newPlayer.getAttributes().getInstance(attribute);

        if (oldInstance != null && newInstance != null) {
            newInstance.setBaseValue(oldInstance.getBaseValue());
        }
    }

}