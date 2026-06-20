package com.goetypunk.addon.mixin;

import com.goetypunk.addon.client.ClientContext;
import com.goetypunk.addon.common.time.TimeStopUtils;
import net.minecraft.client.tutorial.Tutorial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tutorial.class)
public class TutorialMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void gp$tick(CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            ci.cancel();
    }
}
