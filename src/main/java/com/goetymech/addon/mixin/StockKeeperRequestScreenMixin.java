package com.goetymech.addon.mixin;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;

@Mixin(StockKeeperRequestScreen.class)
public class StockKeeperRequestScreenMixin {

    @Shadow(remap = false)
    StockTickerBlockEntity blockEntity;

    @Shadow(remap = false)
    WeakReference<LivingEntity> stockKeeper;

    @Inject(method = "containerTick", at = @At("HEAD"))
    private void gp$patchRemoteStockKeeper(CallbackInfo ci) {
        if (stockKeeper.get() == null
                && blockEntity.getLevel() != null
                && blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            stockKeeper = new WeakReference<>(Minecraft.getInstance().player);
        }
    }
}
