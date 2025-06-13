// 변경된 부분 주석 처리함

package com.ansan.ansanpack.gui;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeChanceManager;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.item.ModItems;
import com.ansan.ansanpack.network.MessageUpgradeChanceSync;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class UpgradeItemHandler extends ItemStackHandler implements Container {

    private final Player ownerPlayer;

    // 🔧 플레이어를 전달받는 생성자
    public UpgradeItemHandler(Player owner) {
        super(2);
        this.ownerPlayer = owner;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot == 0) {
            return stack.isDamageableItem();
        } else if (slot == 1) {
            return stack.getItem() == ModItems.REINFORCE_STONE.get();
        }
        return false;
    }

    @Override public int getContainerSize() { return 2; }
    @Override public boolean isEmpty() {
        for (int i = 0; i < getSlots(); i++) {
            if (!getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }
    @Override public ItemStack getItem(int slot) { return getStackInSlot(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { return extractItem(slot, amount, false); }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getStackInSlot(slot);
        setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }
    @Override public void setItem(int slot, ItemStack stack) { setStackInSlot(slot, stack); }

    @Override
    public void setChanged() {
        if (ownerPlayer instanceof ServerPlayer serverPlayer) {
            ItemStack item = getStackInSlot(0); // 강화 대상 아이템
            if (!item.isEmpty()) {
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item.getItem());
                int level = WeaponUpgradeSystem.getCurrentLevel(item);
                double chance = UpgradeChanceManager.getSuccessChance(itemId, level);

                Optional<UpgradeConfigManager.UpgradeConfig> configOpt = UpgradeConfigManager.getConfig(item.getItem());
                int maxLevel = configOpt.map(c -> c.maxLevel).orElse(0);

                //AnsanPack.LOGGER.debug("[DEBUG] 확률 전송: {}, 레벨: {}, 확률: {}, 최대레벨: {}", itemId, level, chance, maxLevel);

                AnsanPack.NETWORK.sendTo(
                        new MessageUpgradeChanceSync(itemId.toString(), level, chance, maxLevel),
                        serverPlayer.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT
                );
            }
        }
    }

    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() {
        for (int i = 0; i < getSlots(); i++) {
            setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
