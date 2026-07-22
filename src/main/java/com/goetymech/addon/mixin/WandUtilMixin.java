package com.goetymech.addon.mixin;

import com.Polarice3.Goety.utils.WandUtil;
import com.goetymech.addon.common.utils.PhantomWandManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WandUtil.class)
/**
 * Goety 魔杖工具 Mixin。适配晶匣自动施法。
 */
public class WandUtilMixin {

    @Inject(method = "findWand", at = @At("RETURN"), cancellable = true, remap = false)
    private static void gp$checkPhantomWand(LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) return;
        if (!(livingEntity instanceof Player player)) return;
        ItemStack phantom = PhantomWandManager.get(player.getUUID());
        if (!phantom.isEmpty()) {
            cir.setReturnValue(phantom);
        }
    }
}
