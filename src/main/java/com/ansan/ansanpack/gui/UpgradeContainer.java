// 변경된 부분 주석 처리함

package com.ansan.ansanpack.gui;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.item.ModItems;
import com.ansan.ansanpack.network.MessageUpgradeResult;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import com.ansan.ansanpack.config.UpgradeConfigManager;

import java.util.Optional;

public class UpgradeContainer extends AbstractContainerMenu {

    private final UpgradeItemHandler itemHandler; // 🔧 생성자에서 초기화
    public final Slot upgradeSlot;
    public final Slot reinforceStoneSlot;

    public UpgradeContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory);
    }

    public UpgradeContainer(int windowId, Inventory playerInventory) {
        super(AnsanPack.UPGRADE_CONTAINER.get(), windowId);

        // 🔧 여기서 플레이어 객체를 UpgradeItemHandler에 전달
        this.itemHandler = new UpgradeItemHandler(playerInventory.player);

        // 강화 슬롯
        this.upgradeSlot = this.addSlot(new Slot(itemHandler, 0, 55, 40) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.isDamageableItem();
            }
        });

        // 강화석 슬롯
        this.reinforceStoneSlot = this.addSlot(new Slot(itemHandler, 1, 104, 40) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == ModItems.REINFORCE_STONE.get();
            }
        });

        // 플레이어 인벤토리
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // 플레이어 핫바
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;

            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }

    public boolean upgradeItem(Player player) {
        ItemStack weapon = this.upgradeSlot.getItem();
        ItemStack stone = this.reinforceStoneSlot.getItem();

        if (!weapon.isEmpty() && !stone.isEmpty()) {
            Optional<UpgradeConfigManager.UpgradeConfig> config = UpgradeConfigManager.getConfig(weapon.getItem());
            if (config.isPresent()) {
                int currentLevel = WeaponUpgradeSystem.getCurrentLevel(weapon);

                if (currentLevel >= config.get().maxLevel) return false;

                boolean result = WeaponUpgradeSystem.tryUpgrade(weapon, stone);
                AnsanPack.NETWORK.sendTo(
                        new MessageUpgradeResult(result),
                        ((ServerPlayer) player).connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT
                );
                return result;
            }
        }
        return false;
    }

    public Slot getUpgradeSlot() { return this.upgradeSlot; }
    public Slot getReinforceStoneSlot() { return this.reinforceStoneSlot; }

    @Override
    public void removed(Player player) {
        super.removed(player);
        for (int i = 0; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            if (slot.hasItem()) {
                ItemStack itemStack = slot.remove(slot.getItem().getCount());
                player.getInventory().placeItemBackInInventory(itemStack);
            }
        }
    }
}
