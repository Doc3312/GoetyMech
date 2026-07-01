package com.goetymech.addon.mixin;

import com.goetymech.addon.common.time.TimeStopEntityData;
import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void gp$tickTimeStopCountdown(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide) return;
        if (TimeStopEntityData.getTimeStopCount(self) > 0) {
            TimeStopEntityData.setTimeStopCount(self, TimeStopEntityData.getTimeStopCount(self) - 1);
            if (TimeStopEntityData.getTimeStopCount(self) <= 0) {
                TimeStopUtils.use(false, self);
            }
        }
    }
}
