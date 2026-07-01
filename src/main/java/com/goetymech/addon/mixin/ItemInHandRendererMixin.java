package com.goetymech.addon.mixin;

import com.goetymech.addon.client.ClientContext;
import com.goetymech.addon.common.time.TimeContext;
import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @ModifyVariable(method = "renderHandsWithItems", at = @At("HEAD"), argsOnly = true)
    private float gp$renderHandsWithItems(float value) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension
                && Minecraft.getInstance().player != null
                && TimeStopUtils.canMove(Minecraft.getInstance().player))
            return TimeContext.Client.timer.partialTick;
        return value;
    }
}
