package com.goetypunk.addon.mixin;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.item.PrototypeItem;
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
public abstract class PrototypeTravelMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void gp$onTravelHead(Vec3 travelVec, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!PrototypeItem.hasAnyPrototype(player)) return;

        if (player.isFallFlying()) {
            float forward = PrototypeItem.getFlightForward(player);
            if (forward > 0.01f) {
                float factor = PrototypeItem.getFlightSpeedFactor(player);
                if (factor < 1.0f) factor = 1.0f;
                if (!player.level().isClientSide) {
                    double maxMult = ModConfig.FLIGHT_MAX_MULTIPLIER.get();
                    double accel = ModConfig.FLIGHT_ACCEL_RATE.get();
                    factor = Math.min((float)(factor + accel), (float)maxMult);
                    PrototypeItem.setFlightSpeedFactor(player, factor);
                }

                double speed = ModConfig.FLIGHT_TARGET_SPEED.get() * factor;
                Vec3 look = player.getLookAngle();
                Vec3 current = player.getDeltaMovement();
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
        }
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void gp$onTravelTail(Vec3 travelVec, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (PrototypeItem.hasAnyPrototype(player) && player.isFallFlying() && !player.level().isClientSide) {
            float forward = PrototypeItem.getFlightForward(player);
            if (forward <= 0.01f) {
                float factor = PrototypeItem.getFlightSpeedFactor(player);
                if (factor < 1.0f) factor = 1.0f;
                if (factor > 1.0f) {
                    double decel = ModConfig.FLIGHT_DECEL_RATE.get();
                    factor = Math.max(1.0f, (float)(factor - decel));
                    PrototypeItem.setFlightSpeedFactor(player, factor);
                }
            }
        }

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
        player.setDeltaMovement(velocity);
    }
}
