package com.goetypunk.addon.mixin;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.client.ClientWallRunState;
import com.goetypunk.addon.common.item.PrototypeItem;
import com.goetypunk.addon.config.ModConfig;
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
        }

        double speed = ModConfig.WALL_RUN_SPEED.get();
        Vec3 climbVec = wallUp.scale(wallFwd * speed);
        Vec3 strafeVec = wallRight.scale(wallStrafe * speed);
        Vec3 wallMove = climbVec
                .add(strafeVec)
                .add(wallN.scale(-ModConfig.WALL_GRAVITY.get()))
                .add(wallN.scale(ModConfig.WALL_PULL_SPEED.get()));

        if (wallFwd > 0) {
            AABB futureBox = player.getBoundingBox().move(climbVec).inflate(-0.01);
            boolean blocked = !player.level().noCollision(futureBox);
            if (blocked) {
                GoetyPunkMod.LOGGER.info("STEPUP: climb blocked, trying sidestep");
                for (double side : new double[]{0.6, -0.6, 0.4, -0.4, 0.2, -0.2}) {
                    Vec3 sideStep = wallRight.scale(side);
                    AABB checkBox = player.getBoundingBox().move(climbVec.add(sideStep)).inflate(-0.01);
                    if (player.level().noCollision(checkBox)) {
                        GoetyPunkMod.LOGGER.info("STEPUP: sidestep={} OK", side);
                        wallMove = wallMove.add(sideStep);
                        break;
                    }
                }
            }
        }

        Vec3 prePos = player.position();
        player.setDeltaMovement(wallMove);
        player.move(MoverType.SELF, wallMove);
        Vec3 moved = player.position().subtract(prePos);
        GoetyPunkMod.LOGGER.info("WALLMOVE: expected=({},{},{}), actual=({},{},{})",
                String.format("%.3f", wallMove.x), String.format("%.3f", wallMove.y), String.format("%.3f", wallMove.z),
                String.format("%.3f", moved.x), String.format("%.3f", moved.y), String.format("%.3f", moved.z));

        ci.cancel();
    }
}
