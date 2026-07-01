package com.goetymech.addon.common.time;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.BooleanSupplier;

public class LevelExpandedContext {
    public final Level level;

    public LevelExpandedContext(Level level) {
        this.level = level;
    }

    public void tickHead(BooleanSupplier booleanSupplier, CallbackInfo ci) {
    }
}
