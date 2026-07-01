package com.goetymech.addon.client;

import com.Polarice3.Goety.Goety;
import com.goetymech.addon.goetymechMod;
import com.goetymech.addon.common.block.entity.BreedingNestBlockEntity;
import com.goetymech.addon.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = goetymechMod.MOD_ID, value = Dist.CLIENT)
public class BreedingNestHUD {

    private static final int BAR_WIDTH = 64;
    private static final int BAR_HEIGHT = 16;
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 32;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id() != VanillaGuiOverlay.CROSSHAIR.id()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) return;
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = blockHit.getBlockPos();
        double dist = player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        if (dist > 64.0D) return;
        if (!player.level().isLoaded(pos)) return;

        BlockState state = player.level().getBlockState(pos);
        if (!state.is(ModBlocks.BREEDING_NEST.get())) return;

        if (!(player.level().getBlockEntity(pos) instanceof BreedingNestBlockEntity nest)) return;

        GuiGraphics gui = event.getGuiGraphics();
        Font font = mc.font;
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        drawNestHUD(gui, font, width, height, nest);
    }

    private static void drawNestHUD(GuiGraphics gui, Font font, int width, int height, BreedingNestBlockEntity nest) {
        String title = Component.translatable("info.goetymech.hud.nest").getString();
        int titleWidth = font.width(title);
        gui.drawString(font, title, (width - titleWidth) / 2, height - 100, 0xFFFFFF);

        String progress = nest.getEnergy() + "/" + nest.getMaxEnergy() + " hunger";
        int progressWidth = font.width(progress);
        gui.drawString(font, progress, (width - progressWidth) / 2, height - 90, 0xAAAAAA);

        gui.blit(Goety.location("textures/gui/train_bar.png"),
                (width - BAR_WIDTH) / 2, height - 86,
                0, 0, BAR_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        int fill = (int) (BAR_WIDTH * nest.getProgress());
        gui.blit(Goety.location("textures/gui/train_bar.png"),
                (width - BAR_WIDTH) / 2, height - 86,
                0, 16, fill, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        String percent = (int) (nest.getProgress() * 100) + "%";
        gui.drawString(font, percent, (width - font.width(percent)) / 2, height - 86 + 4, 0xFFFFFF);
    }
}
