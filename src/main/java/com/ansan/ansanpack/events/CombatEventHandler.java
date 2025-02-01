package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatEventHandler {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player) {
            Player player = (Player) event.getSource().getEntity();
            ItemStack weapon = player.getMainHandItem();

            if (!weapon.isEmpty()) {
                UpgradeConfigManager.getConfig(weapon.getItem()).ifPresent(config -> {
                    int level = WeaponUpgradeSystem.getCurrentLevel(weapon);
                    if (level > 0 && config.effects.containsKey("damage_per_level")) {
                        double damageBonus = config.effects.get("damage_per_level") * level;
                        event.setAmount(event.getAmount() + (float)damageBonus);
                    }
                });
            }
        }
    }
}
