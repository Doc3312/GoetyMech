package com.goetymech.addon.mixin;

import net.minecraft.client.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
/**
 * Timer.msPerTick 访问器。
 */
public interface TimerAccessor {
    @Accessor("msPerTick")
    float gp$getMsPerTick();

    @Mutable
    @Accessor("msPerTick")
    void gp$setMsPerTick(float value);
}
