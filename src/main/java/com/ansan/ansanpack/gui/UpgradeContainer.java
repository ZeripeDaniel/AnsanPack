package com.ansan.ansanpack.gui;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.item.ModItems;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import java.util.Optional;

public class UpgradeContainer extends AbstractContainerMenu {
    private final UpgradeItemHandler itemHandler = new UpgradeItemHandler();
    private final Slot upgradeSlot;
    private final Slot reinforceStoneSlot;

    public UpgradeContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory);
    }

    public UpgradeContainer(int windowId, Inventory playerInventory) {
        super(AnsanPack.UPGRADE_CONTAINER.get(), windowId);

        // 강화 슬롯
        this.upgradeSlot = this.addSlot(new Slot(itemHandler, 0, 80, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.isDamageableItem();
            }
        });
        this.reinforceStoneSlot = this.addSlot(new Slot(itemHandler, 1, 80, 50) {
            @Override
            public boolean mayPlace(ItemStack stack) {
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

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    public boolean upgradeItem() {
        ItemStack weapon = this.upgradeSlot.getItem();
        ItemStack stone = this.reinforceStoneSlot.getItem();

        if (!weapon.isEmpty() && !stone.isEmpty()) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(weapon.getItem());
            Optional<UpgradeConfigManager.ItemConfig> configOpt = UpgradeConfigManager.getItemConfig(itemId);

            if (configOpt.isPresent()) {
                boolean success = WeaponUpgradeSystem.tryUpgrade(weapon, stone);
                return success;
            }
        }
        return false;
    }

    public Slot getUpgradeSlot() {
        return this.upgradeSlot;
    }

    public Slot getReinforceStoneSlot() {
        return this.reinforceStoneSlot;
    }
}
