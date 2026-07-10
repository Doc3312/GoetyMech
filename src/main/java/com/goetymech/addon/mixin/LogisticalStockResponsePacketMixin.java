package com.goetymech.addon.mixin;

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
public class LogisticalStockResponsePacketMixin {

    @Shadow(remap = false)
    private BlockPos pos;

    @Shadow(remap = false)
    private List<BigItemStack> items;

    @Shadow(remap = false)
    private boolean lastPacket;

    @Inject(method = "handleClient", at = @At("HEAD"), cancellable = true, remap = false)
    private void gp$redirectToRemoteBE(CallbackInfo ci) {
        if (Minecraft.getInstance().level != null
                && Minecraft.getInstance().level.getBlockEntity(pos) instanceof StockTickerBlockEntity) {
            return;
        }

        if (Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.containerMenu instanceof StockKeeperRequestMenu menu) {
            StockTickerBlockEntity stbe = menu.contentHolder;
            if (stbe != null && stbe.getBlockPos().equals(pos)) {
                stbe.receiveStockPacket(items, lastPacket);
                ci.cancel();
            }
        }
    }
}
