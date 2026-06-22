package com.goetypunk.addon.mixin;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.StormWingItem;
import com.goetypunk.addon.config.ModConfig;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class StormWingTravelMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void gp$onTravelHead(Vec3 travelVec, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.isFallFlying()) return;
        if (!StormWingItem.hasAnyStormWing(player)) return;

        float forward = StormWingItem.getFlightForward(player);
        if (!(forward > 0.01f)) return;

        Vec3 look = player.getLookAngle();
        Vec3 current = player.getDeltaMovement();
        double speed = ModConfig.FLIGHT_TARGET_SPEED.get();
        double hoverLift = ModConfig.FLIGHT_HOVER_LIFT.get();
        Vec3 motion = look.scale(speed).add(0, hoverLift, 0);

        // 水平惯性保持：不因低头/抬头削减水平速度
        double currH = current.horizontalDistance();
        double newH = motion.horizontalDistance();

        if (newH < 0.001) {
            motion = new Vec3(current.x, motion.y, current.z);
        } else if (currH > newH && currH > 0.01) {
            double scale = currH / newH;
            motion = new Vec3(motion.x * scale, motion.y, motion.z * scale);
        }

        player.setDeltaMovement(motion);
        player.move(MoverType.SELF, motion);
        ci.cancel();
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void gp$onTravelTail(Vec3 travelVec, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.isFallFlying()) return;
        if (!StormWingItem.hasAnyStormWing(player)) return;

        float forward = StormWingItem.getFlightForward(player);
        if (forward > 0.01f) return;

        double baseAccel = ModConfig.FLIGHT_BASE_ACCEL.get();
        if (!(baseAccel > 0.001)) return;

        Vec3 look = player.getLookAngle();
        Vec3 lookH = new Vec3(look.x, 0, look.z);
        double len = lookH.length();
        if (len < 0.01) return;
        lookH = lookH.scale(1.0 / len);

        Vec3 current = player.getDeltaMovement();
        double hoverLift = ModConfig.FLIGHT_HOVER_LIFT.get();
        Vec3 velocity = current.add(lookH.scale(baseAccel)).add(0, hoverLift, 0);
        player.setDeltaMovement(velocity);
    }
}
