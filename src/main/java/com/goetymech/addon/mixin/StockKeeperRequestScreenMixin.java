package com.goetymech.addon.mixin;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StockKeeperRequestScreen.class)
public class StockKeeperRequestScreenMixin {

    @Shadow(remap = false)
    StockTickerBlockEntity blockEntity;

    @Inject(method = "containerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;closeContainer()V"), cancellable = true)
    private void gp$preventRemoteClose(CallbackInfo ci) {
        if (blockEntity.getLevel() != null
                && blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            ci.cancel();
        }
    }
}
