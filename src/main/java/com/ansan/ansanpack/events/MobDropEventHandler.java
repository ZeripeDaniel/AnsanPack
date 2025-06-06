package com.ansan.ansanpack.events;

import com.ansan.ansanpack.config.MobDropManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class MobDropEventHandler {

    private static final Random random = new Random();

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Monster mob)) return;
        if (!(event.getEntity().level() instanceof ServerLevel)) return;

        ResourceLocation entityType = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (entityType == null) return;

        List<MobDropManager.DropEntry> applicableDrops = MobDropManager.getDropsForEntity(entityType);
        for (MobDropManager.DropEntry entry : applicableDrops) {
            if (random.nextDouble() < entry.chance()) {
                ItemStack drop = new ItemStack(ForgeRegistries.ITEMS.getValue(entry.itemId()), entry.count());
                mob.spawnAtLocation(drop);
            }
        }
    }
}
