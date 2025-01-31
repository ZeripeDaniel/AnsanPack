package com.ansan.ansanpack.gui;

import com.ansan.ansanpack.AnsanPack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;


public class UpgradeScreen extends AbstractContainerScreen<UpgradeContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AnsanPack.MODID, "textures/gui/upgrade_gui.png");

    private Button upgradeButton;


    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        upgradeButton = addRenderableWidget(Button.builder(Component.literal("강화"), button -> {
            // 버튼 클릭 시 실행될 코드
        }).bounds(x + 70, y + 70, 60, 20).build());

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
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

}
