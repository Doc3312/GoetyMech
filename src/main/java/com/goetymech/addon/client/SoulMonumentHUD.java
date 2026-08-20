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
/**
 * 魂灵丰碑 HUD。准星对准时显示绑定信息。
 */
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

        if (state.getValue(com.goetymech.addon.common.block.SoulMonumentBlock.HALF)
                == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = player.level().getBlockState(pos);
        }

        if (!(player.level().getBlockEntity(pos) instanceof SoulMonumentBlockEntity monument)) return;

        GuiGraphics gui = event.getGuiGraphics();
        Font font = mc.font;
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        String text = Component.translatable("info.goetymech.soul_monument.capacity",
                monument.getUsedSlots(), monument.getCapacity()).getString();
        int textWidth = font.width(text);
        gui.drawString(font, text, (width - textWidth) / 2, height - 86, 0xAAAAAA);

        java.util.List<String> names = monument.getSavedEntityNames();
        if (names != null && !names.isEmpty()) {
            int y = height - 100;
            for (int i = names.size() - 1; i >= 0; i--) {
                String name = names.get(i);
                int nameWidth = font.width(name);
                gui.drawString(font, name, (width - nameWidth) / 2, y, 0xCCAAFF);
                y -= 10;
            }
        }

        if (monument.isRecording()) {
            String recordingText = Component.translatable("info.goetymech.soul_monument.already_recording").getString();
            int recWidth = font.width(recordingText);
            gui.drawString(font, recordingText, (width - recWidth) / 2, height - 100, 0xFFAA44);
        }
    }
}
