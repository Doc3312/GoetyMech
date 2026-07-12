package com.goetymech.addon.mixin;

import com.goetymech.addon.GoetyMechMod;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderRequestPacket;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(BlockEntityConfigurationPacket.class)
public class BlockEntityConfigurationPacketMixin {

    @Shadow(remap = false)
    protected BlockPos pos;

    @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    private void gp$bypassRangeCheck(NetworkEvent.Context context, CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object) this) instanceof PackageOrderRequestPacket)) return;

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isSpectator()) return;
            if (player.getServer() == null) return;

            BlockEntity be = null;
            ServerLevel foundInLevel = null;

            be = player.level().getBlockEntity(pos);
            if (be instanceof StockTickerBlockEntity) {
                foundInLevel = (ServerLevel) player.level();
            } else {
                for (ServerLevel sl : player.getServer().getAllLevels()) {
                    if (sl == player.level()) continue;
                    be = sl.getBlockEntity(pos);
                    if (be instanceof StockTickerBlockEntity) {
                        foundInLevel = sl;
                        break;
                    }
                }
            }

            if (be instanceof StockTickerBlockEntity stbe) {
                GoetyMechMod.LOGGER.info("STOCK_DIAG:ORDER player=({}:{},{},{}) → ticker=({},{},{}) → BE found in {}",
                        player.level().dimension().location(),
                        player.getBlockX(), player.getBlockY(), player.getBlockZ(),
                        pos.getX(), pos.getY(), pos.getZ(),
                        foundInLevel.dimension().location());
                try {
                    Method m = PackageOrderRequestPacket.class
                            .getDeclaredMethod("applySettings", ServerPlayer.class, StockTickerBlockEntity.class);
                    m.setAccessible(true);
                    m.invoke(this, player, stbe);
                    stbe.sendData();
                    stbe.setChanged();
                } catch (Exception ignored) {
                }
            } else {
                GoetyMechMod.LOGGER.warn("STOCK_DIAG:ORDER player=({}:{},{},{}) → ticker=({},{},{}) → BE NOT FOUND in any dimension",
                        player.level().dimension().location(),
                        player.getBlockX(), player.getBlockY(), player.getBlockZ(),
                        pos.getX(), pos.getY(), pos.getZ());
            }
        });
        cir.setReturnValue(true);
    }
}
