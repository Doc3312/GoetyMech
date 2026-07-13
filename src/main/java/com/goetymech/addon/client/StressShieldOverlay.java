package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.capabilities.StressShieldProvider;
import com.goetymech.addon.common.utils.StressShieldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT)
public class StressShieldOverlay {
    public static final IGuiOverlay OVERLAY = StressShieldOverlay::drawOverlay;

    private static final float FLASH_DURATION = 10;

    private static float prevShield;

    private static void drawOverlay(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        var cap = player.getCapability(StressShieldProvider.CAPABILITY).orElse(null);
        if (cap == null) return;

        float current = cap.getShield();
        float max = cap.getMaxShield();
        long expireTick = cap.getExpireTick();
        long gameTime = player.level().getGameTime();

        if (expireTick > 0 && gameTime > expireTick) {
            prevShield = 0;
            return;
        }

        if (current <= 0) {
            if (prevShield > 0 && gameTime - cap.getLastHurtTick() < FLASH_DURATION) {
                drawBreakFlash(guiGraphics, screenWidth, screenHeight);
            }
            prevShield = 0;
            return;
        }

        float ratio = max > 0 ? Math.min(current / max, 1.0f) : 0;
        drawEdgeGlow(guiGraphics, screenWidth, screenHeight, ratio);

        float flashAlpha = 0;
        if (current < prevShield) {
            flashAlpha = Math.min(0.3f, (prevShield - current) / max * 0.5f);
            drawHitFlash(guiGraphics, screenWidth, screenHeight, flashAlpha);
        }

        prevShield = current;
    }

    private static void drawEdgeGlow(GuiGraphics gui, int w, int h, float ratio) {
        float alpha = (1.0f - ratio) * 0.3f;
        if (alpha < 0.01f) return;

        int color = (int)(alpha * 255) << 24 | 0x44CCFF;

        int edgeSize = 12;
        gui.fillGradient(0, 0, w, edgeSize, color, 0);
        gui.fillGradient(0, h - edgeSize, w, h, 0, color);
        gui.fillGradient(0, 0, edgeSize, h, color, 0);
        gui.fillGradient(w - edgeSize, 0, w, h, 0, color);
    }

    private static void drawHitFlash(GuiGraphics gui, int w, int h, float alpha) {
        int color = (int)(alpha * 255) << 24 | 0x88DDFF;
        gui.fill(0, 0, w, h, color);
    }

    private static void drawBreakFlash(GuiGraphics gui, int w, int h) {
        gui.fill(0, 0, w, h, 0x30FFFFFF);
    }
}
