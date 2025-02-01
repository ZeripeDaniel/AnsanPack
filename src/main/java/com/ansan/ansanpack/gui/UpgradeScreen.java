package com.ansan.ansanpack.gui;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.config.UpgradeConfigManager;
import com.ansan.ansanpack.gui.UpgradeContainer;
import com.ansan.ansanpack.network.MessageUpgradeRequest;
import com.ansan.ansanpack.upgrade.WeaponUpgradeSystem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;


public class UpgradeScreen extends AbstractContainerScreen<UpgradeContainer> {

    private Component resultText = Component.empty(); // 이 줄 추가
    private static final ResourceLocation TEXTURE = new ResourceLocation(AnsanPack.MODID, "textures/gui/upgrade_gui.png");

    private Button upgradeButton;


    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int buttonX = x + imageWidth - 34;
        int buttonY = y + 8;

        upgradeButton = this.addRenderableWidget(Button.builder(Component.literal("강화"), button -> {
            this.tryUpgrade();
        }).bounds(buttonX, buttonY, 29, 17).build());
    }
    public UpgradeScreen(UpgradeContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 버튼 렌더링
        int buttonX = x + imageWidth - 34;
        int buttonY = y + 8;
        int buttonU = 176; // 버튼 텍스처의 U 좌표 (GUI 텍스처 내에서의 X 위치)
        int buttonV = upgradeButton.isHoveredOrFocused() ? 17 : 0; // 호버 상태에 따라 V 좌표 변경
        guiGraphics.blit(TEXTURE, buttonX, buttonY, buttonU, buttonV, 29, 17);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // 버튼 위에 마우스를 올렸을 때 툴팁 렌더링
        if (upgradeButton.isHovered()) {
            guiGraphics.renderTooltip(this.font, Component.literal("아이템 강화"), mouseX, mouseY);
        }
    }

    public void handleUpgradeResult(boolean success) {
        this.resultText = success ?
                Component.literal("강화 성공!").withStyle(ChatFormatting.GREEN) :
                Component.literal("강화 실패").withStyle(ChatFormatting.RED);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack stack = menu.getUpgradeSlot().getItem();
        UpgradeConfigManager.getConfig(stack.getItem()).ifPresent(config -> {
            String chanceText = String.format("성공률: %.1f%%", WeaponUpgradeSystem.getUpgradeChance(stack)*100);
            guiGraphics.drawString(font, chanceText, 10, 40, 0xFFFFFF);
        });
    }

    private void tryUpgrade() {
        // 클라이언트에서 서버로 강화 요청 전송
        AnsanPack.NETWORK.sendToServer(new MessageUpgradeRequest());
    }


}
