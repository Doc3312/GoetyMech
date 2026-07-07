package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT)
public class FreecamHUD {

    public static final IGuiOverlay OVERLAY = FreecamHUD::drawHUD;

    public static void drawHUD(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.gameMode.getPlayerMode() != GameType.SPECTATOR) return;

        Font font = mc.font;
        if (font == null) return;

        String text = "\u6309R\u9000\u51FA\u795E\u884C\u72B6\u6001";
        int textWidth = font.width(text);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - 48;
        guiGraphics.drawString(font, text, x, y, 0xFF0000);
    }
}
