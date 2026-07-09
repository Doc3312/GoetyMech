package com.goetymech.addon.mixin;

import com.goetymech.addon.client.CrossDimStockCache;
import com.goetymech.addon.common.time.TimeStopUtils;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Consumer;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "guardEntityTick", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void gp$guardEntityTick(Consumer<T> consumer, T entity, CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && TimeStopUtils.andSameDimension((Level) (Object) this)) {
            if (entity == null) {
                ci.cancel();
                return;
            }
            if (!TimeStopUtils.canMove(entity)) {
                ci.cancel();
            }
            return;
        }
    }

    @Inject(method = "getBlockEntity", at = @At("RETURN"), cancellable = true)
    private void gp$onGetBlockEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        if (cir.getReturnValue() != null) return;
        StockTickerBlockEntity cached = CrossDimStockCache.get(pos);
        if (cached != null) cir.setReturnValue(cached);
    }
}
