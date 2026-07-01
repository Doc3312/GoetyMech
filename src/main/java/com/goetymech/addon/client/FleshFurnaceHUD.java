package com.goetymech.addon.client;

import com.Polarice3.Goety.Goety;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.block.entity.FleshFurnaceBlockEntity;
import com.goetymech.addon.config.ModConfig;
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

/**
 * 血肉熔炉 HUD 覆盖层
 * 参考 Goety ClientEvents.renderArcaAmount() 的实现模式
 * 玩家准星对准熔炉时显示转化进度条和兵种链接信息
 */
@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT)
public class FleshFurnaceHUD {

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
        if (!state.is(ModBlocks.FLESH_FURNACE.get())) return;

        if (!(player.level().getBlockEntity(pos) instanceof FleshFurnaceBlockEntity furnace)) return;

        GuiGraphics gui = event.getGuiGraphics();
        Font font = mc.font;
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        if (furnace.isCooking()) {
            drawCookingHUD(gui, font, width, height, furnace);
        } else if (furnace.hasLink()) {
            drawLinkInfo(gui, font, width, height, furnace);
        } else {
            drawNoLinkInfo(gui, font, width, height);
        }
    }

    /**
     * 绘制转化进度（使用 Goety train_bar.png 纹理）
     */
    private static void drawCookingHUD(GuiGraphics gui, Font font, int width, int height, FleshFurnaceBlockEntity furnace) {
        // 转化中标题（等同 Goety 的 "训练中"）
        String title = Component.translatable("info.goetymech.hud.cooking").getString();
        int titleWidth = font.width(title);
        gui.drawString(font, title, (width - titleWidth) / 2, height - 100, 0xFFFFFF);

        // 目标兵种（等同 Goety 的训练目标显示）
        String targetName = furnace.getTargetEntityName();
        if (!targetName.isEmpty()) {
            String entityText = Component.translatable("info.goetymech.hud.linked").getString() + targetName;
            int entityWidth = font.width(entityText);
            gui.drawString(font, entityText, (width - entityWidth) / 2, height - 90, 0xAAAAAA);
        }

        // 进度条（使用 Goety train_bar.png）
        // 上半层：背景（v=0）
        gui.blit(Goety.location("textures/gui/train_bar.png"),
                (width - BAR_WIDTH) / 2, height - 86,
                0, 0, BAR_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // 下半层：已转化填充（v=16）
        int fill = (int) (BAR_WIDTH * furnace.getCookingProgress());
        gui.blit(Goety.location("textures/gui/train_bar.png"),
                (width - BAR_WIDTH) / 2, height - 86,
                0, 16, fill, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // 百分比
        String percent = furnace.getProgressPercent() + "%";
        gui.drawString(font, percent, (width - font.width(percent)) / 2, height - 86 + 4, 0xFFFFFF);
    }

    /**
     * 空闲但已链接状态
     */
    private static void drawLinkInfo(GuiGraphics gui, Font font, int width, int height, FleshFurnaceBlockEntity furnace) {
        String targetName = furnace.getTargetEntityName();
        if (!targetName.isEmpty()) {
            String linkedText = Component.translatable("info.goetymech.hud.linked").getString() + targetName;
            int textWidth = font.width(linkedText);
            gui.drawString(font, linkedText, (width - textWidth) / 2, height - 86, 0xAAAAAA);
        } else {
            // 已链接但火炬未设置训练目标
            String noTargetText = Component.translatable("info.goetymech.hud.linked").getString()
                    + Component.translatable("info.goetymech.furnace.no_target").getString();
            int textWidth = font.width(noTargetText);
            gui.drawString(font, noTargetText, (width - textWidth) / 2, height - 86, 0x666666);
        }
    }

    /**
     * 未链接状态 — 暗影墓碑风格面板提示
     */
    private static void drawNoLinkInfo(GuiGraphics gui, Font font, int width, int height) {
        String line1 = Component.translatable("info.goetymech.furnace.no_link").getString();
        String line2 = Component.translatable("info.goetymech.furnace.no_link_hint", ModConfig.cachedLinkRange).getString();

        int w1 = font.width(line1);
        int w2 = font.width(line2);
        int panelWidth = Math.max(w1, w2) + 20;
        int panelHeight = 38;
        int x = (width - panelWidth) / 2;
        int y = height - 96;

        // 半透明黑色背景面板（暗影墓碑风格）
        gui.fill(x, y, x + panelWidth, y + panelHeight, 0x99333333);
        // 上边线：灵魂火焰红色
        gui.fill(x, y, x + panelWidth, y + 1, 0xFFAA4444);

        // 警告图标 + 主文字（白色）
        gui.drawString(font, "!" + line1, x + 8, y + 6, 0xFFFFFF);
        // 辅助提示（灰色，含范围）
        gui.drawString(font, line2, x + 12, y + 21, 0x888888);
    }
}