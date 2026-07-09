package com.goetymech.addon.mixin;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.item.PrototypeItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PrototypePlayerMixin extends LivingEntity {

    protected PrototypePlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void gp$logAiStepHead(CallbackInfo ci) {
        //GoetyMechMod.LOGGER.debug("MIXIN_AISTEP_HEAD: zza={}, jumping={}, onGround={}, sprint={}",
        //        this.zza, this.jumping, this.onGround(), ((Player)(Object)this).isSprinting());
    }

    @Inject(method = "aiStep", at = @At("RETURN"))
    private void gp$logAiStepReturn(CallbackInfo ci) {
        //GoetyMechMod.LOGGER.debug("MIXIN_AISTEP_RETURN: zza={}, jumping={}, onGround={}, sprint={}",
        //        this.zza, this.jumping, this.onGround(), ((Player)(Object)this).isSprinting());
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void gp$cancelJumpWhileCharging(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (PrototypeItem.getJumpChargeTick(player) > 0) {
            //GoetyMechMod.LOGGER.debug("Prototype Mixin: cancelled jump (charge={})",
            //        PrototypeItem.getJumpChargeTick(player));
            ci.cancel();
            return;
        }
        if (this.jumping && PrototypeItem.hasAnyPrototype(player)) {
            //GoetyMechMod.LOGGER.debug("Prototype Mixin: cancelled jump (jumping=true, hasPrototype=true)");
            ci.cancel();
        }
    }
}
