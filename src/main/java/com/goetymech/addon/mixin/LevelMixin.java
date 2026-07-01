package com.goetymech.addon.mixin;

import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "guardEntityTick", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void gp$guardEntityTick(Consumer<T> consumer, T entity, CallbackInfo ci) {
        if (TimeStopUtils.isTimeStop && TimeStopUtils.andSameDimension((Level) (Object) this)) {
            if (entity == null) {
                ci.cancel();
                return;
            }
            if (!TimeStopUtils.canMove(entity)) {
                ci.cancel();
            }
            return;
        }
    }
}
