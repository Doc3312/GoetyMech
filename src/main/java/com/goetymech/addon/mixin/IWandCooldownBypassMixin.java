package com.goetymech.addon.mixin;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.goetymech.addon.common.item.SquadFocus;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DarkWand.class, remap = false)
public class IWandCooldownBypassMixin {

    @Redirect(method = "use", at = @At(value = "INVOKE",
            target = "Lcom/Polarice3/Goety/api/items/magic/IWand;cannotCast(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Z"),
            remap = false)
    private boolean gp$bypassUse(IWand self, LivingEntity entity, ItemStack stack) {
        return bypass(self, entity, stack);
    }

    @Redirect(method = "finishUsingItem", at = @At(value = "INVOKE",
            target = "Lcom/Polarice3/Goety/api/items/magic/IWand;cannotCast(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Z"),
            remap = false)
    private boolean gp$bypassFinish(IWand self, LivingEntity entity, ItemStack stack) {
        return bypass(self, entity, stack);
    }

    private static boolean bypass(IWand self, LivingEntity entity, ItemStack stack) {
        if (entity.isShiftKeyDown() && entity instanceof Player) {
            ItemStack focus = IWand.getFocus(stack);
            if (focus.getItem() instanceof SquadFocus && SquadFocus.isBound(focus)) {
                return false;
            }
        }
        return self.cannotCast(entity, stack);
    }
}
