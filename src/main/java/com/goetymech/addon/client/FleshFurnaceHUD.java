package com.goetymech.addon.client;

import com.Polarice3.Goety.Goety;
import com.goetymech.addon.goetymechMod;
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
 * 琛€鑲夌啍鐐?HUD 瑕嗙洊灞? * 鍙傝€?Goety ClientEvents.renderArcaAmount() 鐨勫疄鐜版ā寮? * 鐜╁鍑嗘槦瀵瑰噯鐔旂倝鏃舵樉绀鸿浆鍖栬繘搴︽潯鍜屽叺绉嶉摼鎺ヤ俊鎭? */
@Mod.EventBusSubscriber(modid = goetymechMod.MOD_ID, value = Dist.CLIENT)
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
     * 缁樺埗杞寲杩涘害锛堜娇鐢?Goety train_bar.png 绾圭悊锛?     */
    private static void drawCookingHUD(GuiGraphics gui, Font font, int width, int height, FleshFurnaceBlockEntity furnace) {
        // 杞寲涓爣棰橈紙绛夊悓 Goety 鐨?"璁粌涓?锛?
        String title = Component.translatable("info.goetymech.hud.cooking").getString();
        int titleWidth = font.width(title);
        gui.drawString(font, title, (width - titleWidth) / 2, height - 100, 0xFFFFFF);

        // 鐩爣鍏电锛堢瓑鍚?Goety 鐨勮缁冪洰鏍囨樉绀猴級
        String targetName = furnace.getTargetEntityName();
        if (!targetName.isEmpty()) {
    
        String entityText = Component.translatable("info.goetymech.hud.linked").getString() + targetName;
            int entityWidth = font.width(entityText);
            gui.drawString(font, entityText, (width - entityWidth) / 2, height - 90, 0xAAAAAA);
        }

        // 杩涘害鏉★紙浣跨敤 Goety train_bar.png锛?        // 涓婂崐灞傦細鑳屾櫙锛坴=0锛?
        gui.blit(Goety.location("textures/gui/train_bar.png"),
                (width - BAR_WIDTH) / 2, height - 86,
                0, 0, BAR_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // 涓嬪崐灞傦細宸茶浆鍖栧～鍏咃紙v=16锛?
        int fill = (int) (BAR_WIDTH * furnace.getCookingProgress());

        gui.blit(Goety.location("textures/gui/train_bar.png"),
                (width - BAR_WIDTH) / 2, height - 86,
                0, 16, fill, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // 鐧惧垎姣?
        String percent = furnace.getProgressPercent() + "%";
        gui.drawString(font, percent, (width - font.width(percent)) / 2, height - 86 + 4, 0xFFFFFF);
    }

    /**
     * 绌洪棽浣嗗凡閾炬帴鐘舵€?     */
    private static void drawLinkInfo(GuiGraphics gui, Font font, int width, int height, FleshFurnaceBlockEntity furnace) {
        String targetName = furnace.getTargetEntityName();
        if (!targetName.isEmpty()) {
            String linkedText = Component.translatable("info.goetymech.hud.linked").getString() + targetName;
            int textWidth = font.width(linkedText);
            gui.drawString(font, linkedText, (width - textWidth) / 2, height - 86, 0xAAAAAA);
        } else {
            // 宸查摼鎺ヤ絾鐏偓鏈缃缁冪洰鏍?    
        String noTargetText = Component.translatable("info.goetymech.hud.linked").getString()
                    + Component.translatable("info.goetymech.furnace.no_target").getString();
            int textWidth = font.width(noTargetText);
            gui.drawString(font, noTargetText, (width - textWidth) / 2, height - 86, 0x666666);
        }
    }

    /**
     * 鏈摼鎺ョ姸鎬?鈥?鏆楀奖澧撶椋庢牸闈㈡澘鎻愮ず
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

        // 鍗婇€忔槑榛戣壊鑳屾櫙闈㈡澘锛堟殫褰卞纰戦鏍硷級
        gui.fill(x, y, x + panelWidth, y + panelHeight, 0x99333333);
        // 涓婅竟绾匡細鐏甸瓊鐏劙绾㈣壊
        gui.fill(x, y, x + panelWidth, y + 1, 0xFFAA4444);

        // 璀﹀憡鍥炬爣 + 涓绘枃瀛楋紙鐧借壊锛?
        gui.drawString(font, "!" + line1, x + 8, y + 6, 0xFFFFFF);
        // 杈呭姪鎻愮ず锛堢伆鑹诧紝鍚寖鍥达級

        gui.drawString(font, line2, x + 12, y + 21, 0x888888);
    }
}