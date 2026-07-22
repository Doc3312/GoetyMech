package com.goetymech.addon.common.utils;

import com.Polarice3.Goety.api.entities.IOwned;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import java.util.EnumSet;

/**
 * 丰碑仆从跟随 AI。非 IServant 实体跟随玩家。
 */
public class MonumentFollowGoal extends Goal {

    private final Mob mob;
    private final double speedModifier;
    private final float stopDistance;
    private final float startDistance;
    private LivingEntity owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public MonumentFollowGoal(Mob mob, double speedModifier, float startDistance, float stopDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(this.mob instanceof IOwned owned)) return false;
        LivingEntity owner = owned.getTrueOwner();
        if (owner == null || owner.isSpectator()) return false;
        if (this.mob.distanceToSqr(owner) < (double)(this.startDistance * this.startDistance)) return false;
        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.getNavigation().isDone()) return false;
        return this.mob.distanceToSqr(this.owner) > (double)(this.stopDistance * this.stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER);
        this.mob.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
        this.mob.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mob.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            PathNavigation nav = this.mob.getNavigation();
            if (!nav.isInProgress()) {
                nav.moveTo(this.owner, this.speedModifier);
            }
        }
    }
}
