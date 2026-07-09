package com.goetymech.addon.mixin;

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

    private static final double VAULT_CHECK_HEIGHT = 0.8;
    private static final double[] OUTWARD_DISTANCES = {0.9, 1.2, 0.7, 0.5};
    private static final double[] SIDESTEP_VALUES = {0.6, -0.6, 0.4, -0.4, 0.2, -0.2};

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void gp$onWallRunTravel(Vec3 travelVec, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide) return;
        if (!ClientWallRunState.isActive()) return;
        if (!PrototypeItem.hasAnyPrototype(player)) return;

        Vec3 wallN = ClientWallRunState.wallNormal;
        Vec3 wallUp = new Vec3(0, 1, 0);
        Vec3 wallRight = wallUp.cross(wallN).normalize();

        float wallFwd = ClientWallRunState.forwardInput;
        float wallStrafe = ClientWallRunState.strafeInput;

        if (ClientWallRunState.sprintDown || player.isSprinting()) {
            player.setSprinting(true);
            ClientWallRunState.sprintDown = true;
        }

        if (wallFwd > 0.01f) {
            if (ClientWallRunState.wallRunTicks < 200) ClientWallRunState.wallRunTicks++;
        } else if (ClientWallRunState.wallRunTicks > 0) {
            ClientWallRunState.wallRunTicks--;
        }

        int kills = PrototypeItem.getAssimilationKills(player);
        double tierMult = PrototypeItem.getTierMultiplier(kills);
        double ramp = Math.min(ClientWallRunState.wallRunTicks / 80.0, 1.0);
        double maxMult = ModConfig.WALL_RUN_MAX_MULTIPLIER.get();
        double speedMult = 1.0 + ramp * (maxMult - 1.0);
        double speed = ModConfig.WALL_RUN_SPEED.get() * speedMult * tierMult;

        Vec3 climbVec = wallUp.scale(wallFwd * speed);
        Vec3 strafeVec = wallRight.scale(wallStrafe * speed);
        Vec3 wallMove = climbVec
                .add(strafeVec)
                .add(new Vec3(0, -ModConfig.WALL_GRAVITY.get(), 0))
                .add(wallN.scale(-ModConfig.WALL_PULL_SPEED.get()));

        if (wallFwd > 0) {
            AABB playerBox = player.getBoundingBox().inflate(-0.02);
            AABB futureBox = playerBox.move(0, VAULT_CHECK_HEIGHT, 0);
            if (!player.level().noCollision(futureBox)) {
                Vec3 checkBase = new Vec3(0, VAULT_CHECK_HEIGHT, 0);
                boolean found = false;
                for (double out : OUTWARD_DISTANCES) {
                    Vec3 outward = wallN.scale(out);
                    AABB checkBox = playerBox.move(checkBase.add(outward));
                    if (player.level().noCollision(checkBox)) {
                        wallMove = wallMove.add(outward);
                        ClientWallRunState.applyGrace(3);
                        ClientWallRunState.sprintRetainTicks = 30;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    for (double side : SIDESTEP_VALUES) {
                        Vec3 sideStep = wallRight.scale(side);
                        AABB checkBox = playerBox.move(checkBase.add(sideStep));
                        if (player.level().noCollision(checkBox)) {
                            wallMove = wallMove.add(sideStep);
                            break;
                        }
                    }
                }
            }
        }

        player.setDeltaMovement(wallMove);
        player.move(MoverType.SELF, wallMove);

        ci.cancel();
    }
}
