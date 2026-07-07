package com.goetymech.addon.mixin;

import com.goetymech.addon.client.ClientContext;
import com.goetymech.addon.client.FreecamHandler;
import com.goetymech.addon.common.time.TimeContext;
import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
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
    private static float gp$partialTick;

    @ModifyVariable(method = "setup", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float gp$partial(float value) {
        gp$partialTick = value;
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
        Vec3 prev = fh.getPrevCameraPos();
        Vec3 curr = fh.getCameraPos();
        if (prev != null) {
            double x = Mth.lerp(gp$partialTick, prev.x, curr.x);
            double y = Mth.lerp(gp$partialTick, prev.y, curr.y);
            double z = Mth.lerp(gp$partialTick, prev.z, curr.z);
            setPosition(new Vec3(x, y, z));
        } else {
            setPosition(curr);
        }
        float yaw = Mth.rotLerp(gp$partialTick, fh.getPrevYaw(), fh.getYaw());
        float pitch = Mth.lerp(gp$partialTick, fh.getPrevPitch(), fh.getPitch());
        setRotation(yaw, pitch);
    }
}
