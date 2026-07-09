package com.goetymech.addon.mixin;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.goetymech.addon.common.item.SquadFocus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DarkWand.class)
public class DarkWandUseMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = false)
    private void gp$squadFocusShiftDismiss(Level worldIn, Player playerIn, InteractionHand handIn,
                                           CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!playerIn.isShiftKeyDown()) return;

        ItemStack stack = playerIn.getItemInHand(handIn);
        ItemStack focus = IWand.getFocus(stack);
        if (!(focus.getItem() instanceof SquadFocus)) return;
        if (!SquadFocus.isBound(focus)) return;

        if (!worldIn.isClientSide) {
            BlockPos pos = SquadFocus.getBoundPos(focus);
            String dim = SquadFocus.getBoundDimension(focus);
            if (pos != null && dim != null) {
                String key = SquadFocus.makeSummonerKey(pos, dim);
                boolean isMonument = SquadFocus.BOUND_TYPE_MONUMENT.equals(SquadFocus.getBoundType(focus));
                for (Entity entity : ((ServerLevel) worldIn).getAllEntities()) {
                    if (entity instanceof IOwned owned && owned.getTrueOwner() == playerIn) {
                        if (key.equals(entity.getPersistentData().getString(SquadFocus.TAG_SUMMONER))) {
                            if (isMonument) {
                                entity.discard();
                            } else {
                                owned.dismiss();
                            }
                        }
                    }
                }
            }
        }
        playerIn.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 1.0F);
        cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, worldIn.isClientSide()));
    }
}
