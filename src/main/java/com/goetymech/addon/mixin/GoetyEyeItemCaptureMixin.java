package com.goetymech.addon.mixin;

import com.Polarice3.Goety.common.entities.projectiles.EyeItemEntity;
import com.goetymech.addon.common.utils.GatewayUtil;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EyeItemEntity.class)
public class GoetyEyeItemCaptureMixin {
    @Inject(method = "signalTo", at = @At("HEAD"), remap = false)
    private void gp$capturePos(BlockPos pos, CallbackInfo ci) {
        if (GatewayUtil.pendingEyeCapture) {
            GatewayUtil.pendingEyePos = pos;
        }
    }
}
