package com.goetymech.addon.mixin;

import com.goetymech.addon.common.item.PrototypeItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
/**
 * 原型步高 Mixin。提升原型持有者的步伐高度。
 */
public abstract class PrototypeStepHeightMixin {

    @Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
    private void gp$overrideMaxUpStep(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Player player && PrototypeItem.hasAnyPrototype(player)) {
            cir.setReturnValue(1.5f);
        }
    }
}
