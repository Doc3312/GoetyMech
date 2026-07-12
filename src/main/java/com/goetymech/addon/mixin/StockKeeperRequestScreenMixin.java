package com.goetymech.addon.mixin;

import com.goetymech.addon.GoetyMechMod;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Shadow(remap = false)
    public AddressEditBox addressBox;

    @Unique
    private int goetymech$refreshTick = 0;

    @Inject(method = "containerTick", at = @At("HEAD"))
    private void gp$patchRemoteStockKeeperAndRefresh(CallbackInfo ci) {
        if (stockKeeper.get() == null
                && blockEntity.getLevel() != null
                && blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            stockKeeper = new WeakReference<>(Minecraft.getInstance().player);
        }

        if (blockEntity.getLevel() != null
                && blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            goetymech$refreshTick++;
            if (goetymech$refreshTick % 20 == 0) {
                GoetyMechMod.LOGGER.info("STOCK_DIAG:REFRESH tick={} player=({}:{},{},{}) → fakeBE=({},{},{})",
                        goetymech$refreshTick,
                        blockEntity.getLevel().dimension().location(),
                        Minecraft.getInstance().player.getBlockX(),
                        Minecraft.getInstance().player.getBlockY(),
                        Minecraft.getInstance().player.getBlockZ(),
                        blockEntity.getBlockPos().getX(),
                        blockEntity.getBlockPos().getY(),
                        blockEntity.getBlockPos().getZ());
                blockEntity.refreshClientStockSnapshot();
            }
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void gp$prefillAddress(CallbackInfo ci) {
        if (blockEntity.getLevel() != null
                && blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            String playerName = Minecraft.getInstance().player.getGameProfile().getName();
            addressBox.setValue(playerName);
        }
    }

    @Inject(method = "removed", at = @At("TAIL"), remap = false)
    private void gp$cleanupRemoteBE(CallbackInfo ci) {
        if (blockEntity != null && blockEntity.getLevel() != null
                && blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            blockEntity.invalidateCaps();
        }
    }
}
