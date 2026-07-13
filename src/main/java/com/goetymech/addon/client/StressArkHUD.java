package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT)
public class StressArkHUD {

    private static final int BAR_WIDTH = 128;
    private static final int BAR_HEIGHT = 9;

    private static final ResourceLocation ENGINE_TEXTURE =
            new ResourceLocation(GoetyMechMod.MOD_ID, "textures/gui/engine_energy.png");

    private static float totalCapacity;
    private static float totalStress;
    private static int networkCount;
    private static float engineConsumption;
    private static boolean hasData;

    public static final IGuiOverlay OVERLAY = StressArkHUD::drawHUD;

    public static void onData(float cap, float stress, int networks, float consumption) {
        totalCapacity = cap;
        totalStress = stress;
        networkCount = networks;
        engineConsumption = consumption;
        hasData = true;
    }

    public static boolean hasArk() {
        return hasData;
    }

    public static float getTotalCapacity() {
        return totalCapacity;
    }

    public static float getTotalStress() {
        return totalStress;
    }

    public static void clearData() {
        hasData = false;
        totalCapacity = 0;
        totalStress = 0;
        networkCount = 0;
        engineConsumption = 0;
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
        float cap = totalCapacity;
        float stress = totalStress;
        float used = Math.max(stress, 0);
        // totalStress（used）已含远程引擎消耗（Reader.calculateStressApplied 注入 Create 网络），
        // 不再重复扣减 engineConsumption

        int barX = width / 2 + 100;
        int barY = height - 25;

        RenderSystem.setShaderTexture(0, ENGINE_TEXTURE);

        // 1. 棕色背景条
        gui.blit(ENGINE_TEXTURE,
                barX, barY, 0, 9, BAR_WIDTH, BAR_HEIGHT, 128, 90);

        if (cap > 0) {
            // 2. 绿色底条（始终全满）
            gui.blit(ENGINE_TEXTURE,
                    barX + 9, barY + 2, 0, 21, 117, 5, 128, 90);

            // 3. 红色覆盖条（按 used/cap 比例）
            int redWidth = (int) (117 * Math.min(1.0f, used / cap));
            gui.blit(ENGINE_TEXTURE,
                    barX + 9, barY + 2, 0, 29, redWidth, 5, 128, 90);
        }

        // 文字水平居中
        String text = (int) used + "/" + (int) cap + " SU";
        int textX = barX + 9 + (117 - font.width(text)) / 2;
        int textY = barY + 1;
        gui.drawString(font, text, textX, textY, used > cap ? 0xFF5555 : 0xFFFFFF);

    }
}
