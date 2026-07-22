package com.goetymech.addon.mixin;

import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.InstantNeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InstantNeighborUpdater.class)
/**
 * 红石更新 Mixin（即时模式）。时停中取消更新。
 */
public abstract class InstantNeighborUpdaterMixin {
    @Shadow @Final private Level level;

    @Inject(method = {"neighborChanged*", "shapeUpdate"}, at = @At("HEAD"), cancellable = true)
    private void gp$cancelWhenTimeFrozen(CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && TimeStopUtils.andSameDimension(this.level))
            ci.cancel();
    }
}
