package com.goetymech.addon.mixin;

import com.goetymech.addon.client.ClientContext;
import com.goetymech.addon.client.FreecamHandler;
import com.goetymech.addon.common.time.TimeContext;
import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow protected void setPosition(Vec3 pos) {}
    @Shadow protected void setRotation(float yRot, float xRot) {}

    private static boolean gp$freecamOverride;

    @ModifyVariable(method = "setup", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float gp$partial(float value) {
        FreecamHandler fh = FreecamHandler.getInstance();
        if (fh.isActive()) {
            gp$freecamOverride = true;
            return value;
        }
        gp$freecamOverride = false;
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension) {
            var p = Minecraft.getInstance().player;
            if (p != null) {
                return TimeStopUtils.canMove(p) ? TimeContext.Client.timer.partialTick : 0F;
            }
        }
        return value;
    }

    @Inject(method = "setup", at = @At("RETURN"))
    private void gp$afterSetup(CallbackInfo ci) {
        if (!gp$freecamOverride) return;
        FreecamHandler fh = FreecamHandler.getInstance();
        if (!fh.isActive()) return;
        setPosition(fh.getCameraPos());
        setRotation(fh.getYaw(), fh.getPitch());
    }
}
