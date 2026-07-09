package com.goetymech.addon.mixin;

import com.simibubi.create.content.logistics.stockTicker.PackageOrderRequestPacket;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
            Level world = player.level();
            if (world == null || !world.isLoaded(pos)) return;
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof StockTickerBlockEntity stbe) {
                try {
                    Method m = PackageOrderRequestPacket.class
                            .getDeclaredMethod("applySettings", ServerPlayer.class, StockTickerBlockEntity.class);
                    m.setAccessible(true);
                    m.invoke(this, player, stbe);
                    stbe.sendData();
                    stbe.setChanged();
                } catch (Exception ignored) {
                }
            }
        });
        cir.setReturnValue(true);
    }
}
