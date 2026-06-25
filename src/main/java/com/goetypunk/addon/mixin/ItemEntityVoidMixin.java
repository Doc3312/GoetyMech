package com.goetypunk.addon.mixin;

import com.goetypunk.addon.common.item.PrototypeItem;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityVoidMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void gp$voidProtectPrototype(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (!PrototypeItem.isPrototype(self.getItem())) return;
        if (self.getY() < -64.0) {
            self.setPos(self.getX(), 260.0, self.getZ());
            self.setDeltaMovement(0, 0, 0);
        }
    }
}
