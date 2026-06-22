package com.goetypunk.addon.mixin;

import com.goetypunk.addon.common.item.StormWingItem;
import com.goetypunk.addon.config.ModConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class StormWingTravelMixin {

    @Inject(method = "travel", at = @At("HEAD"))
    private void gp$onTravelHead(Vec3 travelVec, CallbackInfo ci) {
        if (!((Object) this instanceof Player player)) return;
        if (!player.isFallFlying()) return;
        if (!StormWingItem.hasAnyStormWing(player)) return;

        Vec3 motion = player.getDeltaMovement();
        float forward = StormWingItem.getFlightForward(player);
        if (forward > 0.01f) {
            Vec3 look = player.getLookAngle();
            motion = motion.add(look.scale(forward * ModConfig.FLIGHT_BOOST_FORCE.get()));
        }
        double lift = ModConfig.FLIGHT_HOVER_LIFT.get();
        if (lift > 0) {
            motion = motion.add(0, lift, 0);
        }
        player.setDeltaMovement(motion);
    }
}
