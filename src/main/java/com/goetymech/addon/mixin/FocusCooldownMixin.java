package com.goetymech.addon.mixin;

import com.Polarice3.Goety.common.capabilities.soulenergy.FocusCooldown;
import com.goetymech.addon.common.item.SquadFocus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FocusCooldown.class)
public class FocusCooldownMixin {

    @Inject(method = "removeCooldown", at = @At("HEAD"), cancellable = true, remap = false)
    private void gp$protectSquadFocusCooldown(Player player, Level level, Item item, CallbackInfo ci) {
        if (item instanceof SquadFocus && !level.isClientSide) {
            ci.cancel();
        }
    }
}
