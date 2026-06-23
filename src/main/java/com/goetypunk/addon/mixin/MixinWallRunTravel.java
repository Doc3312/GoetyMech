package com.goetypunk.addon.mixin;

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
        Vec3 wallMove = wallUp.scale(wallFwd * speed)
                .add(wallRight.scale(wallStrafe * speed))
                .add(wallN.scale(-ModConfig.WALL_GRAVITY.get()))
                .add(wallN.scale(ModConfig.WALL_PULL_SPEED.get()));

        Vec3 prePos = player.position();

        player.setDeltaMovement(wallMove);
        player.move(MoverType.SELF, wallMove);

        Vec3 moved = player.position().subtract(prePos);
        boolean climbingBlocked = wallFwd > 0 && moved.y < wallFwd * speed * 0.3;

        if (climbingBlocked) {
            for (double dy : new double[]{0.6, 1.0}) {
                if (player.level().noCollision(player.getBoundingBox().move(0, dy, 0))) {
                    player.setPos(player.getX(), player.getY() + dy, player.getZ());
                    player.move(MoverType.SELF, wallMove);
                    break;
                }
            }
            for (double side : new double[]{0.5, -0.5, 0.3, -0.3}) {
                Vec3 shift = wallRight.scale(side);
                if (player.level().noCollision(player.getBoundingBox().move(shift))) {
                    player.setPos(player.getX() + shift.x, player.getY(), player.getZ() + shift.z);
                    player.move(MoverType.SELF, new Vec3(0, wallFwd * speed, 0));
                    break;
                }
            }
        }

        ci.cancel();
    }
}
