package com.goetypunk.addon.client.gui;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.menu.FleshCasketContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FleshCasketScreen extends AbstractContainerScreen<FleshCasketContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(GoetyPunkMod.MOD_ID, "textures/gui/container/flesh_casket.png");

    public FleshCasketScreen(FleshCasketContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
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
            guiGraphics.blit(GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
        }
    }
}
