package com.goetypunk.addon.mixin;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.StormWingItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class StormWingPlayerMixin extends LivingEntity {

    protected StormWingPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void gp$cancelJumpWhileCharging(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (StormWingItem.getJumpChargeTick(player) > 0) {
            GoetyPunkMod.LOGGER.info("StormWing Mixin: cancelled jump (charge={})",
                    StormWingItem.getJumpChargeTick(player));
            ci.cancel();
            return;
        }
        if (this.jumping && StormWingItem.hasAnyStormWing(player)) {
            GoetyPunkMod.LOGGER.info("StormWing Mixin: cancelled jump (jumping=true, hasStormWing=true)");
            ci.cancel();
        }
    }
}
