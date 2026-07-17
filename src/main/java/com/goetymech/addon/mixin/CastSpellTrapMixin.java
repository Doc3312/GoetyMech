package com.goetymech.addon.mixin;

import com.Polarice3.Goety.common.entities.util.CastSpellTrap;
import com.goetymech.addon.common.utils.PhantomWandManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CastSpellTrap.class)
public class CastSpellTrapMixin {

    @Unique
    private boolean goetymech$skipDiscard = false;

    @Redirect(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lcom/Polarice3/Goety/common/entities/util/CastSpellTrap;discard()V")
    )
    private void gp$preventDiscard(CastSpellTrap trap) {
        if (goetymech$skipDiscard) {
            goetymech$skipDiscard = false;
            return;
        }

        if (trap.getOwner() instanceof Player player) {
            ItemStack phantom = PhantomWandManager.get(player.getUUID());
            if (!phantom.isEmpty()) {
                return;
            }
        }

        goetymech$skipDiscard = true;
        trap.discard();
        goetymech$skipDiscard = false;
    }
}
