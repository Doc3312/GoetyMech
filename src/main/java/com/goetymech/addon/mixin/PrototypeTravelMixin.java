package com.goetymech.addon.mixin;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.config.ModConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PrototypeTravelMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void gp$onTravelHead(Vec3 travelVec, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.isFallFlying()) return;
        if (!PrototypeItem.hasAnyPrototype(player)) return;

        float forward = PrototypeItem.getFlightForward(player);
        if (!(forward > 0.01f)) return;

        Vec3 look = player.getLookAngle();
        Vec3 current = player.getDeltaMovement();
        double base = ModConfig.FLIGHT_TARGET_SPEED.get();
        double accelRate = ModConfig.FLIGHT_ACCEL_RATE.get();
        int kills = PrototypeItem.getAssimilationKills(player);
        double maxMultiplier = ModConfig.FLIGHT_MAX_MULTIPLIER.get() * PrototypeItem.getTierMultiplier(kills);
        double currentLookSpeed = current.dot(look);
        double speed = Mth.clamp(currentLookSpeed + accelRate, base, base * maxMultiplier);
        double hoverLift = speed * 0.16;
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
                motion = new Vec3(0, vMotion.y, 0);
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
        if (!PrototypeItem.hasAnyPrototype(player)) return;

        float forward = PrototypeItem.getFlightForward(player);
        if (forward > 0.01f) return;

        Vec3 look = player.getLookAngle();
        Vec3 current = player.getDeltaMovement();
        double speed = ModConfig.FLIGHT_TARGET_SPEED.get();
        double noWSpeed = speed * 0.8;
        double hoverLift = speed * 0.16;
        double noWHoverLift = hoverLift * 0.8;
        double baseAccel = speed * 0.04;
        if (!(baseAccel > 0.001)) return;

        double pitch = player.getXRot();
        double liftFactor = Mth.clamp(-pitch / 90.0, 0, 1);
        Vec3 lookH = new Vec3(look.x, 0, look.z);
        double len = lookH.length();

        double targetVy;
        Vec3 velocity;
        if (pitch > 0.01) {
            double speedAlongLook = Math.max(noWSpeed, current.dot(look) + 0.08 * (-look.y));
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
            targetVy = look.y * noWSpeed + noWHoverLift * liftFactor;
            if (len < 0.01) {
                player.setDeltaMovement(new Vec3(current.x, targetVy, current.z));
                return;
            }
            lookH = lookH.scale(1.0 / len);
            velocity = current.add(lookH.scale(baseAccel));
            velocity = new Vec3(velocity.x, targetVy, velocity.z);
        }
        double base = ModConfig.FLIGHT_TARGET_SPEED.get();
        double decelRate = ModConfig.FLIGHT_DECEL_RATE.get();
        double currHSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (currHSpeed > base * 1.05 && decelRate < 0.999) {
            double newHSpeed = Math.max(currHSpeed * decelRate, base);
            double factor = newHSpeed / currHSpeed;
            velocity = new Vec3(velocity.x * factor, velocity.y, velocity.z * factor);
        }
        player.setDeltaMovement(velocity);
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void gp$onTravelWater(Vec3 travelVec, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!PrototypeItem.hasAnyPrototype(player)) return;
        if (player.isFallFlying()) return;

        if (player.isInWater()) {
            player.setAirSupply(player.getMaxAirSupply());

            float forward = player.zza;
            if (forward > 0.01f) {
                int tick = PrototypeItem.getSwimTick(player) + 1;
                PrototypeItem.setSwimTick(player, tick);

                int kills = PrototypeItem.getAssimilationKills(player);
                double tierMult = PrototypeItem.getTierMultiplier(kills);
                double desiredSpeedMult = 1.0 + Math.min((tick - 1) / 80.0, 1.0) * ModConfig.SWIM_MAX_MULTIPLIER.get() * tierMult;
                double velocityMult = 1.25 - 0.25 / desiredSpeedMult;

                if (velocityMult > 1.01) {
                    Vec3 motion = player.getDeltaMovement();
                    double hSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
                    if (hSpeed > 0.001) {
                        player.setDeltaMovement(motion.x * velocityMult, motion.y * velocityMult, motion.z * velocityMult);
                    }
                }
            } else {
                PrototypeItem.setSwimTick(player, 0);
            }
        } else {
            PrototypeItem.setSwimTick(player, 0);
        }
    }
}
