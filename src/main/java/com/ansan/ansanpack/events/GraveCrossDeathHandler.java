package com.ansan.ansanpack.events;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.*;

@Mod.EventBusSubscriber(modid = AnsanPack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GraveCrossDeathHandler {

    private static class InventorySnapshot {
        public final List<ItemStack> inventory;
        public final List<ItemStack> armor;
        //public final ItemStack mainHand;
        public final ItemStack offHand;
        public final Map<String, List<ItemStack>> curiosItems;

        public InventorySnapshot(ServerPlayer player) {
            this.inventory = new ArrayList<>();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                inventory.add(player.getInventory().getItem(i).copy());
            }

            this.armor = new ArrayList<>();
            for (ItemStack stack : player.getInventory().armor) {
                armor.add(stack.copy());
            }

            //this.mainHand = player.getMainHandItem().copy();
            this.offHand = player.getOffhandItem().copy();

            // Curios 백업
            this.curiosItems = new HashMap<>();
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                    List<ItemStack> items = new ArrayList<>();
                    for (int i = 0; i < entry.getValue().getSlots(); i++) {
                        items.add(entry.getValue().getStacks().getStackInSlot(i).copy());
                    }
                    curiosItems.put(entry.getKey(), items);
                }
            });
        }

        public void restoreTo(ServerPlayer player) {
            // 1. 먼저 손 비우기 (슬롯 충돌 방지)
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);

            // 2. 인벤토리 클리어
            player.getInventory().clearContent();

            // 3. 인벤토리 복구
            for (int i = 0; i < inventory.size(); i++) {
                if (i < player.getInventory().getContainerSize()) {
                    player.getInventory().setItem(i, inventory.get(i));
                }
            }

            // 4. 갑옷 복구
            for (int i = 0; i < armor.size(); i++) {
                if (i < player.getInventory().armor.size()) {
                    player.getInventory().armor.set(i, armor.get(i));
                }
            }

            // 5. 손 복구 (맨 마지막에!)
            //player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
            player.setItemInHand(InteractionHand.OFF_HAND, offHand);

            // 6. Curios 복구
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                for (Map.Entry<String, List<ItemStack>> entry : curiosItems.entrySet()) {
                    ICurioStacksHandler stackHandler = handler.getCurios().get(entry.getKey());
                    if (stackHandler != null) {
                        for (int i = 0; i < entry.getValue().size(); i++) {
                            if (i < stackHandler.getSlots()) {
                                stackHandler.getStacks().setStackInSlot(i, entry.getValue().get(i));
                            }
                        }
                    }
                }
            });
        }


    }

    private static final Map<UUID, InventorySnapshot> pendingInventory = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean hasTicket = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(ModItems.INVENTORY_SAVE_TICKET.get())) {
                stack.shrink(1);
                hasTicket = true;

                // ✅ 채팅 메시지
                player.displayClientMessage(Component.literal("§a[안내] 세이브권이 사용되었습니다! 휴~"), false);
                break;
            }
        }

        if (!hasTicket) return;

        pendingInventory.put(player.getUUID(), new InventorySnapshot(player));
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.getCurios().forEach((slotType, stackHandler) -> {
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    stackHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                }
            });
        });
        player.getInventory().clearContent(); // 무덤 방지
        event.setCanceled(false);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        InventorySnapshot snapshot = pendingInventory.remove(player.getUUID());
        if (snapshot == null) return;

        snapshot.restoreTo(player);
    }
}
