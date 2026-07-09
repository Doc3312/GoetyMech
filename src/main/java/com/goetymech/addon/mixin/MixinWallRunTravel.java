package com.goetymech.addon.mixin;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.client.ClientWallRunState;
import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.config.ModConfig;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
            ClientWallRunState.sprintDown = true;
        }

        int kills = PrototypeItem.getAssimilationKills(player);
        double tierMult = PrototypeItem.getTierMultiplier(kills);
        double speed = ModConfig.WALL_RUN_SPEED.get() * tierMult;

        double mvSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED) != null
                ? player.getAttribute(Attributes.MOVEMENT_SPEED).getValue() : -1;
        double mvBase = player.getAttribute(Attributes.MOVEMENT_SPEED) != null
                ? player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() : -1;
        boolean hasMod = player.getAttribute(Attributes.MOVEMENT_SPEED) != null
                && player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(
                        java.util.UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890")) != null;
        int sprintTick = PrototypeItem.getSprintTick(player);
        GoetyMechMod.LOGGER.debug("WALL_SPEED: speed={} mvSpeed={} mvBase={} sprintTick={} hasMod={} fwd={} strafe={} wallN=({}) kills={} tierMult={}",
                String.format("%.4f", speed), String.format("%.4f", mvSpeed), String.format("%.4f", mvBase),
                sprintTick, hasMod, wallFwd, wallStrafe,
                String.format("%.2f,%.2f,%.2f", wallN.x, wallN.y, wallN.z),
                kills, String.format("%.3f", tierMult));

        Vec3 climbVec = wallUp.scale(wallFwd * speed);
        Vec3 strafeVec = wallRight.scale(wallStrafe * speed);
        Vec3 wallMove = climbVec
                .add(strafeVec)
                .add(wallN.scale(-ModConfig.WALL_GRAVITY.get()))
                .add(wallN.scale(ModConfig.WALL_PULL_SPEED.get()));

        if (wallFwd > 0) {
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

        player.setDeltaMovement(wallMove);
        player.move(MoverType.SELF, wallMove);

        ci.cancel();
    }
}
