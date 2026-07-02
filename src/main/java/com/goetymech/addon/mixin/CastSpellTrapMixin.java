package com.goetymech.addon.mixin;

import com.Polarice3.Goety.common.entities.util.CastSpellTrap;
import com.goetymech.addon.common.utils.PhantomWandManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CastSpellTrap.class)
public class CastSpellTrapMixin {

    @Inject(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;discard()V"),
        cancellable = true,
        remap = false
    )
    private void gp$preventDiscard(CallbackInfo ci) {
        CastSpellTrap self = (CastSpellTrap)(Object)this;
        if (self.getOwner() instanceof Player player) {
            ItemStack phantom = PhantomWandManager.get(player.getUUID());
            if (!phantom.isEmpty()) {
                ci.cancel();
            }
        }
    }
}
