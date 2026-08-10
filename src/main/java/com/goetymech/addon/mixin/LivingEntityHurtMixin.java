package com.goetymech.addon.mixin;

import com.goetymech.addon.common.time.TimeStopUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
/**
 * 实体受伤 Mixin。时停中取消伤害。
 */
public class LivingEntityHurtMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void gp$cancelHurtIfFrozen(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (TimeStopUtils.isTimeStop && TimeStopUtils.isInRange(self)) {
            if (!(self instanceof Player)) {
                cir.setReturnValue(false);
            }
        }
    }
}
