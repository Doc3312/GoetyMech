package com.goetymech.addon.client;

import com.goetymech.addon.common.item.PrototypeMimicItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class MimicItemDecorator implements IItemDecorator {
    private static final ResourceLocation OVERLAY_TEXTURE =
            new ResourceLocation("goetymech", "textures/item/prototype_mimic_overlay.png");

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!(stack.getItem() instanceof PrototypeMimicItem)) return false;
        if (PrototypeMimicItem.getSourceStack(stack).isEmpty()) return false;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 0);
        guiGraphics.blit(OVERLAY_TEXTURE, xOffset, yOffset, 16, 16, 0, 0, 16, 16, 16, 16);
        guiGraphics.pose().popPose();
        return false;
    }
}
