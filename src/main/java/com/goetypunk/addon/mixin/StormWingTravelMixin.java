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
        float forward = StormWingItem.getFlightForward(player);
        if (!(forward > 0.01f)) return;

        Vec3 look = player.getLookAngle();
        double speed = ModConfig.FLIGHT_TARGET_SPEED.get();
        Vec3 motion = look.scale(speed).add(0, 0.08, 0);
        player.setDeltaMovement(motion);
    }
}
