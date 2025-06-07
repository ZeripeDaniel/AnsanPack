package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import com.ansan.ansanpack.server.stat.PlayerStat;
import com.ansan.ansanpack.server.stat.ServerStatCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID)
public class StatAttributeApplier {

    private static final UUID STR_UUID = UUID.fromString("e1a3b171-11a1-4d9f-82e2-b76adbb52f4b");
    private static final UUID AGI_UUID = UUID.fromString("1a5c0b9e-c3c1-4a8f-8ad7-8714b03240cb");
    private static final UUID LUCK_UUID = UUID.fromString("b1e72c7f-1f7b-4c5c-91d4-4c2f6c1e9a6f");


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        var attrMap = player.getAttributes();
        PlayerStat stat = ServerStatCache.get(player.getUUID());

        // 힘 → 공격력
        double str = stat.getStrength() * 0.5;
        applyModifier(attrMap.getInstance(Attributes.ATTACK_DAMAGE), STR_UUID, "stat.str", str);

        // 민첩 → 이동속도
        double agi = stat.getAgility() * 0.005;
        applyModifier(attrMap.getInstance(Attributes.MOVEMENT_SPEED), AGI_UUID, "stat.agi", agi);

        // 행운 → generic.luck
        double luck = stat.getLuck();;
        applyModifier(attrMap.getInstance(Attributes.LUCK), LUCK_UUID, "stat.luck", luck);
    }

    private static void applyModifier(net.minecraft.world.entity.ai.attributes.AttributeInstance attr,
                                      UUID uuid, String name, double value) {
        if (attr == null) return;

        if (attr.getModifier(uuid) != null) {
            attr.removeModifier(uuid);
        }

        if (value != 0) {
            AttributeModifier modifier = new AttributeModifier(uuid, name, value, AttributeModifier.Operation.ADDITION);
            attr.addPermanentModifier(modifier);
        }
    }
}
