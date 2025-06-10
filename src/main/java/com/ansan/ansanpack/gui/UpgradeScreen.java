package com.ansan.ansanpack.gui;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.network.MessageUpgradeRequest;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class UpgradeScreen extends AbstractContainerScreen<UpgradeContainer> {

    private Component resultText = Component.empty();
    private static final ResourceLocation TEXTURE = new ResourceLocation(AnsanPack.MODID, "textures/gui/upgrade_gui.png");
    private UpgradeButton upgradeButton;
    private double currentChance = 0.0;
    private double wtfchange ;
    private int currentLevel = 0;
    private static double syncedChance = 0.0; // 서버에서 받은 확률 (신뢰값)
    private static int syncedMaxLevel = 0;

    public UpgradeScreen(UpgradeContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.upgradeButton = this.addRenderableWidget(new UpgradeButton(
                x + imageWidth - 40,
                y + 38,
                button -> tryUpgrade()
        ));

        refreshUpgradeInfo();
    }

    private void refreshUpgradeInfo() {
        ItemStack stack = menu.getUpgradeSlot().getItem();
        UpgradeConfigManager.getConfig(stack.getItem()).ifPresent(config -> {
            this.currentLevel = WeaponUpgradeSystem.getCurrentLevel(stack);
            //this.currentChance = WeaponUpgradeSystem.getUpgradeChance(stack);  // ← 확률 메서드 사용
        });
    }

    @Override
    public void containerTick() {
        super.containerTick();
        refreshUpgradeInfo();
        this.menu.slotsChanged(this.menu.getUpgradeSlot().container);
        this.menu.slotsChanged(this.menu.getReinforceStoneSlot().container);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if (upgradeButton != null && upgradeButton.isHovered()) {
            guiGraphics.renderTooltip(this.font,
                    Component.literal("아이템 강화"),
                    mouseX, mouseY
            );
        }

        if (upgradeButton.isActive() &&
                (menu.getUpgradeSlot().getItem().isEmpty() || menu.getReinforceStoneSlot().getItem().isEmpty())) {
            guiGraphics.renderTooltip(font,
                    Component.literal("강화석과 아이템을 모두 넣어주세요!"),
                    mouseX, mouseY
            );
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        refreshUpgradeInfo();

        String chanceText = String.format("성공률: %.2f%%", syncedChance * 100);
        guiGraphics.drawString(font, chanceText, 8, 60, 0xFFFFFF);

        guiGraphics.drawString(
                this.font,
                this.resultText,
                35, 70,
                0xFFFFFF,
                true
        );

            if (currentLevel >= syncedMaxLevel) {
                guiGraphics.drawString(font, "MAX", 90, 60, 0xFFD700);
            }
    }

    public void handleUpgradeResult(boolean success) {
        refreshUpgradeInfo();

        ItemStack weapon = menu.getUpgradeSlot().getItem();
        Optional<UpgradeConfigManager.UpgradeConfig> config = UpgradeConfigManager.getConfig(weapon.getItem());

        if (config.isPresent() && currentLevel >= syncedMaxLevel) {
            resultText = Component.literal("최대 강화 레벨 도달!").withStyle(ChatFormatting.YELLOW);
        } else {
            resultText = success
                    ? Component.literal("강화 성공!").withStyle(ChatFormatting.GREEN)
                    : Component.literal("강화 실패").withStyle(ChatFormatting.RED);
        }

        this.init();
    }
private void tryUpgrade() {
    int upgradeSlotIndex = menu.upgradeSlot.index;
    int stoneSlotIndex = menu.reinforceStoneSlot.index;

    AnsanPack.LOGGER.debug("Sending upgrade request: {}, {}", upgradeSlotIndex, stoneSlotIndex);

    ItemStack weapon = menu.getSlot(upgradeSlotIndex).getItem();
    ItemStack stone = menu.getSlot(stoneSlotIndex).getItem();

    if (weapon.isEmpty() || stone.isEmpty()) {
        resultText = Component.literal("강화할 아이템과 강화석을 넣어주세요!").withStyle(ChatFormatting.RED);
        return;
    }

    if (currentLevel >= syncedMaxLevel) {
        resultText = Component.literal("최대 강화 레벨에 도달했습니다.").withStyle(ChatFormatting.YELLOW);
        return;
    }

    //AnsanPack.LOGGER.debug("에라이싯팔레벨 {}, {}", currentLevel, syncedMaxLevel);
    // 🔥 강화 시도 패킷 전송
    AnsanPack.NETWORK.sendToServer(new MessageUpgradeRequest(upgradeSlotIndex, stoneSlotIndex));
    AnsanPack.LOGGER.debug("패킷 전송 시작: 업그레이드 슬롯={}, 강화석 슬롯={}", upgradeSlotIndex, stoneSlotIndex);
}


    public static class UpgradeButton extends Button {
        private static final ResourceLocation BUTTON_TEXTURE =
                new ResourceLocation("ansanpack", "textures/gui/upgrade_button.png");

        public UpgradeButton(int x, int y, OnPress onPress) {
            super(x, y, 29, 17, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.setShaderTexture(0, BUTTON_TEXTURE);
            int v = this.isHoveredOrFocused() ? 17 : 0;
            guiGraphics.blit(
                    BUTTON_TEXTURE,
                    this.getX(),
                    this.getY(),
                    0, v,
                    29, 17,
                    29, 34
            );
        }
    }

    public static void setChance(String itemId, int level, double chance, int maxLevel) {
        syncedChance = chance;
        syncedMaxLevel = maxLevel;
        AnsanPack.LOGGER.debug("[DEBUG] GUI 확률 적용됨 → 아이템: {}, 레벨: {}, 확률: {}, 최대레벨: {}", itemId, level, chance, maxLevel);
    }


}
