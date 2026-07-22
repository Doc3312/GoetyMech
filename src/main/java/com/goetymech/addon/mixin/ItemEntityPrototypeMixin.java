package com.goetymech.addon.mixin;

import com.goetymech.addon.common.item.PrototypeItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
/**
 * 物品实体 Mixin。原型物品虚空保护。
 */
public class ItemEntityPrototypeMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void gp$protectPrototypeItem(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (PrototypeItem.isPrototype(self.getItem())) {
            cir.setReturnValue(false);
        }
    }
}
