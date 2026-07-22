package com.goetymech.addon.mixin;

import com.goetymech.addon.client.ClientContext;
import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
/**
 * 粒子引擎 Mixin。时停中取消粒子 tick。
 */
public class ParticleEngineMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void gp$freezeParticles(CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            ci.cancel();
    }
}
