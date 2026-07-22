package com.goetymech.addon.common.time;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.BooleanSupplier;

/**
 * Level 扩展上下文。Mixin 注入点在 tick() 开头调用 tickHead() 来取消时停中的世界 tick。
 */
public class LevelExpandedContext {
    public final Level level;

    public LevelExpandedContext(Level level) {
        this.level = level;
    }

    public void tickHead(BooleanSupplier booleanSupplier, CallbackInfo ci) {
    }
}
