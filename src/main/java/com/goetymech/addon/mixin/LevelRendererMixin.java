package com.goetymech.addon.mixin;

import com.goetymech.addon.client.ClientContext;
import com.goetymech.addon.common.time.TimeContext;
import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyVariable(method = "renderEntity", at = @At("HEAD"), argsOnly = true)
    private float gp$modifyEntityPartialTicks(float partialTicks, Entity entity) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension) {
            if (TimeStopUtils.canMove(entity)) {
                return TimeContext.Client.timer.partialTick;
            }
            return 0F;
        }
        return partialTicks;
    }

    @Inject(method = "tickRain", at = @At("HEAD"), cancellable = true)
    private void gp$tickRain(Camera camera, CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            ci.cancel();
    }
}
