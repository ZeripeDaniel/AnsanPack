package com.ansan.ansanpack.client;

import com.ansan.ansanpack.network.MessageSyncMoveDistance;
import com.ansan.ansanpack.AnsanPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, value = Dist.CLIENT)
public class ClientMovementTracker {

    private static Vec3 lastPosition = null;
    private static double accumulatedDistance = 0.0;

    public static void reset() {
        accumulatedDistance = 0.0;
    }

    public static int consumeMovedDistance() {
        int moved = (int) accumulatedDistance;
        accumulatedDistance -= moved;
        return moved;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isAlive()) {
            lastPosition = null;
            return;
        }

        Vec3 currentPos = player.position();
        if (lastPosition != null) {
            double dx = currentPos.x - lastPosition.x;
            double dy = currentPos.y - lastPosition.y;
            double dz = currentPos.z - lastPosition.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            accumulatedDistance += distance;
        }
        lastPosition = currentPos;
    }

    @SubscribeEvent
    public static void onScreenOpened(ScreenEvent.Opening event) {
        if (accumulatedDistance < 1.0) return;
        int moved = consumeMovedDistance();
        if (moved > 0) {
            AnsanPack.NETWORK.sendToServer(new MessageSyncMoveDistance(moved));
        }
    }
}
