package com.goetypunk.addon.mixin;

import com.goetypunk.addon.client.ClientContext;
import com.goetypunk.addon.common.time.TimeContext;
import com.goetypunk.addon.common.time.TimeStopRandom;
import com.goetypunk.addon.common.time.TimeStopUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final @Mutable private RandomSource random;
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gp$init(CallbackInfo ci) {
        this.random = new TimeStopRandom(TimeContext.Client.generateUniqueSeed());
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float gp$render(float value) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            value = TimeContext.Client.timer.partialTick;
        return value;
    }

    @ModifyVariable(method = "renderLevel", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 0),
            ordinal = 0, argsOnly = true)
    private float gp$render_beforeCamera(float value) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            value = TimeContext.Client.timer.partialTick;
        return value;
    }

    @ModifyVariable(method = "renderLevel", at = @At(
            value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onCameraSetup(Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/Camera;F)Lnet/minecraftforge/client/event/ViewportEvent$ComputeCameraAngles;", remap = false),
            ordinal = 0, argsOnly = true)
    private float gp$render_afterCamera(float value) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            value = minecraft.getFrameTime();
        return value;
    }

    @ModifyVariable(method = "renderLevel", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;)V"),
            ordinal = 0, argsOnly = true)
    private float gp$render_beforeLevel(float value) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            value = minecraft.getFrameTime();
        return value;
    }

    @ModifyVariable(method = "bobView", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float gp$bobView(float value) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension && minecraft.player != null) {
            value = TimeStopUtils.canMove(minecraft.player)
                    ? TimeContext.Client.timer.partialTick : minecraft.getFrameTime();
        }
        return value;
    }

    @ModifyVariable(method = "bobHurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float gp$bobHurt(float value) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension && minecraft.player != null) {
            value = TimeStopUtils.canMove(minecraft.player)
                    ? TimeContext.Client.timer.partialTick : minecraft.getFrameTime();
        }
        return value;
    }
}
