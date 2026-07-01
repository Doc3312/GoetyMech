package com.goetymech.addon.mixin;

import com.goetymech.addon.client.ClientContext;
import com.goetymech.addon.common.time.TimeContext;
import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public class CameraMixin {
    @ModifyVariable(method = "setup", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float gp$partial(float value) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension) {
            var p = Minecraft.getInstance().player;
            if (p != null) {
                return TimeStopUtils.canMove(p) ? TimeContext.Client.timer.partialTick : 0F;
            }
        }
        return value;
    }
}
