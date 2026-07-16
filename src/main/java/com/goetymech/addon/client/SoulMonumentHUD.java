package com.goetymech.addon.client;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.block.entity.SoulMonumentBlockEntity;
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

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT)
public class SoulMonumentHUD {

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
        if (!(state.getBlock() instanceof com.goetymech.addon.common.block.SoulMonumentBlock)) return;

        if (!(player.level().getBlockEntity(pos) instanceof SoulMonumentBlockEntity monument)) return;

        GuiGraphics gui = event.getGuiGraphics();
        Font font = mc.font;
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        java.util.List<String> names = monument.getSavedEntityNames();
        int nameCount = names != null ? names.size() : 0;
        boolean recording = monument.isRecording();

        int lineHeight = 10;
        int totalHeight = nameCount * lineHeight + lineHeight;
        if (recording) totalHeight += lineHeight;

        int topY = (height - totalHeight) / 2;
        topY = Math.max(topY, 5);
        if (topY + totalHeight > height - 5) {
            topY = height - 5 - totalHeight;
        }

        int currentY = topY;
        if (names != null && !names.isEmpty()) {
            for (String name : names) {
                int nameWidth = font.width(name);
                gui.drawString(font, name, (width - nameWidth) / 2, currentY, 0xCCAAFF);
                currentY += lineHeight;
            }
        }

        String text = Component.translatable("info.goetymech.soul_monument.capacity",
                monument.getUsedSlots(), monument.getCapacity()).getString();
        int textWidth = font.width(text);
        gui.drawString(font, text, (width - textWidth) / 2, currentY, 0xAAAAAA);
        currentY += lineHeight;

        if (recording) {
            String recordingText = Component.translatable("info.goetymech.soul_monument.already_recording").getString();
            int recWidth = font.width(recordingText);
            gui.drawString(font, recordingText, (width - recWidth) / 2, currentY, 0xFFAA44);
        }
    }
}
