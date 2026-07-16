package com.goetymech.addon.client.gui;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.menu.FleshCasketContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FleshCasketScreen extends AbstractContainerScreen<FleshCasketContainer> {
    private static final ResourceLocation[] GUI_TEXTURES = {
        new ResourceLocation(GoetyMechMod.MOD_ID, "textures/gui/container/flesh_casket_1.png"),
        new ResourceLocation(GoetyMechMod.MOD_ID, "textures/gui/container/flesh_casket_2.png"),
        new ResourceLocation(GoetyMechMod.MOD_ID, "textures/gui/container/flesh_casket_3.png")
    };

    private final int tier;

    public FleshCasketScreen(FleshCasketContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.tier = container.getTier();
    }

    @Override
    protected void init() {
        this.imageHeight = 140;
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.inventoryLabelY = this.imageHeight - 92;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.minecraft != null) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            int idx = Math.max(0, Math.min(tier - 1, GUI_TEXTURES.length - 1));
            guiGraphics.blit(GUI_TEXTURES[idx], i, j, 0, 0, this.imageWidth, this.imageHeight, 176, 140);
        }
    }
}
