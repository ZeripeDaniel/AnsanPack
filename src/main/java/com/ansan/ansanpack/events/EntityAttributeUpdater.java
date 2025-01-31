//package com.ansan.ansanpack.events;
//
//import com.ansan.ansanpack.AnsanPack;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.ai.attributes.Attributes;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.phys.AABB;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import java.util.List;
//
//@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
//public class EntityAttributeUpdater {
//
//    private static final int UPDATE_INTERVAL = 200; // 틱 간격 (10초)
//    private static int tickCounter = 0;
//
//    @SubscribeEvent
//    public static void onWorldTick(TickEvent.LevelTickEvent event) {
//        if (event.phase == TickEvent.Phase.END) {
//            tickCounter++;
//            if (tickCounter >= UPDATE_INTERVAL) {
//                tickCounter = 0;
//
//                // 플레이어 주변의 큰 영역을 정의합니다
//                double range = 100.0; // 원하는 범위로 조정하세요
//                List<Entity> nearbyEntities = event.level.getEntitiesOfClass(Entity.class,
//                        new AABB(event.level.getSharedSpawnPos()).inflate(range));
//
//                for (Entity entity : nearbyEntities) {
//                    if (entity.getType().getCategory().toString().equals("animalistic_a:ninfa")) {
//                        updateEntityAttributes(entity);
//                    }
//                }
//            }
//        }
//    }
//
//    private static void updateEntityAttributes(Entity entity) {
//        if (entity instanceof LivingEntity livingEntity) {
//            if (livingEntity.getAttribute(Attributes.MAX_HEALTH) != null) {
//                livingEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(300.0);
//                livingEntity.setHealth(Math.min(livingEntity.getHealth(), 300.0f));
//            }
//            if (livingEntity.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
//                livingEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3);
//            }
//            if (livingEntity.getAttribute(Attributes.ARMOR) != null) {
//                livingEntity.getAttribute(Attributes.ARMOR).setBaseValue(5.0);
//            }
//            if (livingEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
//                livingEntity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0);
//            }
//        }
//    }
//}
