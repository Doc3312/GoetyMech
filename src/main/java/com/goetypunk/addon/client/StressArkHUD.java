package com.goetypunk.addon.client;

import com.Polarice3.Goety.Goety;
import com.goetypunk.addon.GoetyPunkMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyPunkMod.MOD_ID, value = Dist.CLIENT)
public class StressArkHUD {

    private static final int BAR_WIDTH = 128;
    private static final int BAR_HEIGHT = 9;
    private static final int ENERGY_BAR_HEIGHT = 5;

    private static float totalCapacity;
    private static float totalStress;
    private static int networkCount;
    private static boolean hasData;

    public static final IGuiOverlay OVERLAY = StressArkHUD::drawHUD;

    public static void onData(float cap, float stress, int networks) {
        totalCapacity = cap;
        totalStress = stress;
        networkCount = networks;
        hasData = true;
    }

    public static void clearData() {
        hasData = false;
        totalCapacity = 0;
        totalStress = 0;
        networkCount = 0;
    }

    public static void drawHUD(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
        if (!hasData) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        Font font = mc.font;

        drawStressBar(guiGraphics, font, screenWidth, screenHeight);
    }

    private static void drawStressBar(GuiGraphics gui, Font font, int width, int height) {
        float used = totalStress;
        float cap = totalCapacity;
        float fraction = cap <= 0 ? 0 : used / cap;

        int barX = width / 2 + 50;
        int barY = height - 29;

        int fillWidth = (int) (117 * Math.min(1.0F, fraction));

        RenderSystem.setShaderTexture(0, Goety.location("textures/gui/soul_energy.png"));
        gui.blit(Goety.location("textures/gui/soul_energy.png"),
                barX, barY, 0, 9, BAR_WIDTH, BAR_HEIGHT, 128, 90);

        if (cap > 0) {
            gui.blit(Goety.location("textures/gui/soul_energy.png"),
                    barX + 9, barY, 9, 18, fillWidth, BAR_HEIGHT, 128, 90);
        }

        int offset = (int) ((Minecraft.getInstance().player.tickCount + Minecraft.getInstance().getPartialTick()) % 234);
        RenderSystem.setShaderTexture(0, Goety.location("textures/gui/soul_energy_bar.png"));
        gui.blit(Goety.location("textures/gui/soul_energy_bar.png"),
                barX + 9, barY + 2, offset, 0, fillWidth, ENERGY_BAR_HEIGHT, 234, ENERGY_BAR_HEIGHT);

        String text = (int) used + "/" + (int) cap + " SU";
        int textX = barX + 37;
        int textY = barY + 1;
        gui.drawString(font, text, textX, textY, 0xFFFFFF);
    }
}
