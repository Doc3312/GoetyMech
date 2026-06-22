package com.goetypunk.addon.mixin;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.StormWingItem;
import com.goetypunk.addon.config.ModConfig;
import net.minecraft.util.Mth;
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
        double pitch = player.getXRot();
        double liftFactor = Mth.clamp(-pitch / 90.0, 0, 1);
        Vec3 motion;
        if (pitch > 0.01) {
            double speedAlongLook = Math.max(speed, current.dot(look) + 0.08 * (-look.y));
            Vec3 targetMotion = look.scale(speedAlongLook);
            motion = new Vec3(
                current.x * 0.3 + targetMotion.x * 0.7,
                current.y * 0.3 + targetMotion.y * 0.7,
                current.z * 0.3 + targetMotion.z * 0.7
            );
        } else {
            double targetVy = look.y * speed + hoverLift * liftFactor;
            Vec3 vMotion = new Vec3(look.x * speed, targetVy, look.z * speed);
            double currH = current.horizontalDistance();
            double newH = vMotion.horizontalDistance();
            if (newH < 0.001) {
                motion = new Vec3(current.x, vMotion.y, current.z);
            } else if (currH > newH && currH > 0.01) {
                double scale = currH / newH;
                motion = new Vec3(vMotion.x * scale, vMotion.y, vMotion.z * scale);
            } else {
                motion = vMotion;
            }
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
        Vec3 current = player.getDeltaMovement();
        double speed = ModConfig.FLIGHT_TARGET_SPEED.get();
        double hoverLift = ModConfig.FLIGHT_HOVER_LIFT.get();
        double pitch = player.getXRot();
        double liftFactor = Mth.clamp(-pitch / 90.0, 0, 1);
        Vec3 lookH = new Vec3(look.x, 0, look.z);
        double len = lookH.length();

        double targetVy;
        Vec3 velocity;
        if (pitch > 0.01) {
            double speedAlongLook = Math.max(speed, current.dot(look) + 0.08 * (-look.y));
            Vec3 targetMotion = look.scale(speedAlongLook);
            targetVy = current.y * 0.3 + targetMotion.y * 0.7;
            if (len < 0.01) {
                player.setDeltaMovement(new Vec3(current.x, targetVy, current.z));
                return;
            }
            lookH = lookH.scale(1.0 / len);
            velocity = current.add(lookH.scale(baseAccel));
            velocity = new Vec3(
                current.x * 0.3 + targetMotion.x * 0.7,
                targetVy,
                current.z * 0.3 + targetMotion.z * 0.7
            );
        } else {
            targetVy = look.y * speed + hoverLift * liftFactor;
            if (len < 0.01) {
                player.setDeltaMovement(new Vec3(current.x, targetVy, current.z));
                return;
            }
            lookH = lookH.scale(1.0 / len);
            velocity = current.add(lookH.scale(baseAccel));
            velocity = new Vec3(velocity.x, targetVy, velocity.z);
        }
        player.setDeltaMovement(velocity);
    }
}
