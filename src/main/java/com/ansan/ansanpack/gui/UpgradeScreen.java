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

import java.util.Optional;


public class UpgradeScreen extends AbstractContainerScreen<UpgradeContainer> {

    private Component resultText = Component.empty(); // 이 줄 추가
    private static final ResourceLocation TEXTURE = new ResourceLocation(AnsanPack.MODID, "textures/gui/upgrade_gui.png");

    private UpgradeButton upgradeButton; // 버튼 변수명 일관성 유지


    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 버튼 위치 계산 수정
        int buttonX = x + imageWidth - 40; // GUI 오른쪽에서 38px 왼쪽
        int buttonY = y + 38; // GUI 상단에서 40px 아래
        this.upgradeButton = this.addRenderableWidget(new UpgradeButton(buttonX, buttonY, button -> {
            this.tryUpgrade();
        }));
    }
    public static class UpgradeButton extends Button {
        private static final ResourceLocation BUTTON_TEXTURE =
                new ResourceLocation("ansanpack", "textures/gui/upgrade_button.png");

        public UpgradeButton(int x, int y, OnPress onPress) {
            super(x, y, 29, 17, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 텍스처 바인딩 추가
            RenderSystem.setShaderTexture(0, BUTTON_TEXTURE);

            // UV 좌표 계산 (호버 시 하단 텍스처)
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
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // Null 체크 추가
        if (upgradeButton != null && upgradeButton.isHovered()) {
            guiGraphics.renderTooltip(this.font,
                    Component.literal("아이템 강화"),
                    mouseX, mouseY
            );
        }
    }

    public void handleUpgradeResult(boolean success) {
        ItemStack weapon = menu.getUpgradeSlot().getItem();
        Optional<UpgradeConfigManager.UpgradeConfig> config = UpgradeConfigManager.getConfig(weapon.getItem());

        if (config.isPresent() && WeaponUpgradeSystem.getCurrentLevel(weapon) >= config.get().maxLevel) {
            this.resultText = Component.literal("최대 강화 레벨 도달!").withStyle(ChatFormatting.YELLOW);
        } else {
            this.resultText = success
                    ? Component.literal("강화 성공!").withStyle(ChatFormatting.GREEN)
                    : Component.literal("강화 실패").withStyle(ChatFormatting.RED);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY); // 상위 클래스의 레이블
        // 강화 성공률 표시 위치 조정
        ItemStack stack = menu.getUpgradeSlot().getItem();
        UpgradeConfigManager.getConfig(stack.getItem()).ifPresent(config -> {
            String chanceText = String.format("성공률: %.1f%%",
                    WeaponUpgradeSystem.getUpgradeChance(stack)*100
            );
            guiGraphics.drawString(
                    font,
                    chanceText,
                    8,  // X 좌표
                    60, // Y 좌표
                    0xFFFFFF,
                    false // dropShadow
            );
        });

        // 결과 메시지 위치 조정
        guiGraphics.drawString(
                this.font,
                this.resultText,
                35, // X 좌표
                70, // Y 좌표
                0xFFFFFF,
                true
        );
    }


    private void tryUpgrade() {
        // 강화 가능 여부 사전 체크
        ItemStack weapon = menu.getUpgradeSlot().getItem();
        ItemStack stone = menu.getReinforceStoneSlot().getItem();
        if (weapon.isEmpty() || stone.isEmpty()) {
            resultText = Component.literal("강화할 아이템과 강화석을 넣어주세요!").withStyle(ChatFormatting.RED);
            return;
        }

        // 서버로 패킷 전송
        AnsanPack.NETWORK.sendToServer(new MessageUpgradeRequest(weapon, stone));
    }

}
