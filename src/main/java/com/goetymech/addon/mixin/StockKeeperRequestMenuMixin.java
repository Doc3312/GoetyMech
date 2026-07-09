package com.goetymech.addon.mixin;

import com.goetymech.addon.client.CrossDimStockCache;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StockKeeperRequestMenu.class)
public class StockKeeperRequestMenuMixin {

    @Shadow(remap = false)
    private boolean isAdmin;

    @Shadow(remap = false)
    private boolean isLocked;

    @Inject(method = "createOnClient", at = @At("HEAD"), remap = false, cancellable = true)
    private void gp$createOnClient(FriendlyByteBuf extraData, CallbackInfoReturnable<StockTickerBlockEntity> cir) {
        isAdmin = extraData.readBoolean();
        isLocked = extraData.readBoolean();
        BlockPos pos = extraData.readBlockPos();

        Level level = Minecraft.getInstance().level;
        if (level != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof StockTickerBlockEntity stbe) {
                cir.setReturnValue(stbe);
                return;
            }
        }

        StockTickerBlockEntity cached = new StockTickerBlockEntity(
                AllBlockEntityTypes.STOCK_TICKER.get(),
                pos,
                AllBlocks.STOCK_TICKER.get().defaultBlockState()
        );
        CrossDimStockCache.register(pos, cached);
        cir.setReturnValue(cached);
    }
}
