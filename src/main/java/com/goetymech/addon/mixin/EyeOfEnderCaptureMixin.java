package com.goetymech.addon.mixin;

import com.goetymech.addon.common.utils.GatewayUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EyeOfEnder.class)
public class EyeOfEnderCaptureMixin {
    @Inject(method = "signalTo", at = @At("HEAD"))
    private void gp$capturePos(BlockPos pos, CallbackInfo ci) {
        if (GatewayUtil.pendingEyeCapture) {
            GatewayUtil.pendingEyePos = pos;
        }
    }
}
