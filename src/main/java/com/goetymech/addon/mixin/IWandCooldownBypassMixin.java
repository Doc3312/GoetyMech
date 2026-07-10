package com.goetymech.addon.mixin;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.goetymech.addon.common.item.SquadFocus;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IWand.class, remap = false)
public class IWandCooldownBypassMixin {

    @Inject(method = "isOnCooldown", at = @At("HEAD"), cancellable = true)
    private void gp$bypassSquadFocusShiftCooldown(LivingEntity livingEntity, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity.isShiftKeyDown() && livingEntity instanceof Player) {
            ItemStack focus = IWand.getFocus(stack);
            if (focus.getItem() instanceof SquadFocus && SquadFocus.isBound(focus)) {
                cir.setReturnValue(false);
            }
        }
    }
}
