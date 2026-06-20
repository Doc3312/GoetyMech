package com.goetypunk.addon.mixin;

import com.goetypunk.addon.client.ClientContext;
import com.goetypunk.addon.common.time.TimeContext;
import com.goetypunk.addon.common.time.TimeStopUtils;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
public class GuiMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float gp$partial(float value) {
        if (TimeStopUtils.isTimeStop && ClientContext.isTimeStop_andSameDimension)
            value = TimeContext.Client.timer.partialTick;
        return value;
    }
}
