package com.goetymech.addon.mixin;

import com.goetymech.addon.GoetyMechMod;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockResponsePacket;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LogisticalStockResponsePacket.class)
/**
 * Create 物流响应包 Mixin。适配远程仓储。
 */
public class LogisticalStockResponsePacketMixin {

    @Shadow(remap = false)
    private BlockPos pos;

    @Shadow(remap = false)
    private List<BigItemStack> items;

    @Shadow(remap = false)
    private boolean lastPacket;

    @Inject(method = "handleClient", at = @At("HEAD"), cancellable = true, remap = false)
    private void gp$redirectToRemoteBE(CallbackInfo ci) {
        boolean realBE = Minecraft.getInstance().level != null
                && Minecraft.getInstance().level.getBlockEntity(pos) instanceof StockTickerBlockEntity;
        if (realBE) {
            GoetyMechMod.LOGGER.debug("STOCK_DIAG:STOCK pos=({},{},{}) items={} last={} → realBE",
                    pos.getX(), pos.getY(), pos.getZ(), items.size(), lastPacket);
            return;
        }

        if (Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.containerMenu instanceof StockKeeperRequestMenu menu) {
            StockTickerBlockEntity stbe = menu.contentHolder;
            if (stbe != null && stbe.getBlockPos().equals(pos)) {
                GoetyMechMod.LOGGER.info("STOCK_DIAG:STOCK pos=({},{},{}) items={} last={} → fakeBE",
                        pos.getX(), pos.getY(), pos.getZ(), items.size(), lastPacket);
                stbe.receiveStockPacket(items, lastPacket);
                ci.cancel();
                return;
            }
        }

        GoetyMechMod.LOGGER.debug("STOCK_DIAG:STOCK pos=({},{},{}) items={} last={} → DROPPED (no target)",
                pos.getX(), pos.getY(), pos.getZ(), items.size(), lastPacket);
    }
}
