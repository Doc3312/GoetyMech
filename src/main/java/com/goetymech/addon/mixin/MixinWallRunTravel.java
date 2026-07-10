package com.goetymech.addon.mixin;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.client.ClientWallRunState;
import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.config.ModConfig;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinWallRunTravel {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void gp$onWallRunTravel(Vec3 travelVec, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        boolean isClient = player.level().isClientSide;
        if (!isClient && !PrototypeItem.isWallRunning(player)) return;
        if (isClient && !ClientWallRunState.isActive()) return;
        if (!PrototypeItem.hasAnyPrototype(player)) return;

        Vec3 wallN = isClient ? ClientWallRunState.wallNormal : PrototypeItem.getWallNormal(player);
        if (wallN.lengthSqr() < 0.5 && (isClient ? ClientWallRunState.graceTicks : 0) <= 0) return;
        if (wallN.lengthSqr() < 0.5 && isClient) wallN = ClientWallRunState.lastWallNormal;
        if (wallN.lengthSqr() < 0.5) return;
        wallN = wallN.normalize();
        Vec3 wallUp = new Vec3(0, 1, 0);
        Vec3 wallRight = wallUp.cross(wallN).normalize();

        float wallFwd = isClient ? ClientWallRunState.forwardInput : PrototypeItem.getWallForward(player);
        float wallStrafe = isClient ? ClientWallRunState.strafeInput : PrototypeItem.getWallStrafe(player);

        if (isClient && (ClientWallRunState.sprintDown || player.isSprinting())) {
            player.setSprinting(true);
            ClientWallRunState.sprintDown = true;
        }

        int kills = PrototypeItem.getAssimilationKills(player);
        double tierMult = PrototypeItem.getTierMultiplier(kills);
        double speed = ModConfig.WALL_RUN_SPEED.get() * tierMult;
        Vec3 climbVec = wallUp.scale(wallFwd * speed);
        Vec3 strafeVec = wallRight.scale(wallStrafe * speed);
        Vec3 wallMove = climbVec
                .add(strafeVec)
                .add(wallN.scale(-ModConfig.WALL_GRAVITY.get()))
                .add(wallN.scale(ModConfig.WALL_PULL_SPEED.get()));

        if (wallFwd > 0 && isClient) {
            AABB futureBox = player.getBoundingBox().move(climbVec).inflate(-0.02);
            if (!player.level().noCollision(futureBox)) {
                boolean found = false;
                for (double out : new double[]{2.8, 3.2, 2.5, 2.0}) {
                    Vec3 outward = wallN.scale(-out);
                    AABB checkBox = player.getBoundingBox().move(climbVec.add(outward)).inflate(-0.02);
                    if (player.level().noCollision(checkBox)) {
                        wallMove = wallMove.add(outward);
                        ClientWallRunState.applyGrace(15);
                        ClientWallRunState.sprintRetainTicks = 60;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    for (double side : new double[]{0.6, -0.6, 0.4, -0.4, 0.2, -0.2}) {
                        Vec3 sideStep = wallRight.scale(side);
                        AABB checkBox = player.getBoundingBox().move(climbVec.add(sideStep)).inflate(-0.02);
                        if (player.level().noCollision(checkBox)) {
                            wallMove = wallMove.add(sideStep);
                            break;
                        }
                    }
                }
            }
        }

        GoetyMechMod.LOGGER.info("WALLRUN_MIXIN: side={}, wallN=({},{},{}), fwd={}, strafe={}, sprint={}, isFly={}, onGround={}, speed={}, wallMove=({},{},{})",
                isClient ? "CLIENT" : "SERVER",
                String.format("%.2f", wallN.x), String.format("%.2f", wallN.y), String.format("%.2f", wallN.z),
                wallFwd, wallStrafe, player.isSprinting(), player.isFallFlying(), player.onGround(),
                String.format("%.3f", speed),
                String.format("%.4f", wallMove.x), String.format("%.4f", wallMove.y), String.format("%.4f", wallMove.z));

        player.setDeltaMovement(wallMove);
        player.move(MoverType.SELF, wallMove);

        if (!isClient) {
            player.hurtMarked = true;
        }

        ci.cancel();
    }
}
