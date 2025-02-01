package com.ansan.ansanpack.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class UpgradeItemHandler extends ItemStackHandler implements Container {
    public UpgradeItemHandler() {
        super(2);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.isDamageableItem();
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return getStackInSlot(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getStackInSlot(slot);
        setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        setStackInSlot(slot, stack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        setStackInSlot(0, ItemStack.EMPTY);
    }

}
