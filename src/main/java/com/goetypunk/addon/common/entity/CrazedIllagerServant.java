package com.goetypunk.addon.common.entity;

import com.Polarice3.Goety.common.entities.ally.illager.VindicatorServant;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;

public class CrazedIllagerServant extends VindicatorServant {

    private int particleTicks;

    public CrazedIllagerServant(EntityType<? extends Owned> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.525)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), 15.0);
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MOVEMENT_SPEED), 0.525);
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ATTACK_DAMAGE), 2.0);
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.KNOCKBACK_RESISTANCE), 1.0);
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.FOLLOW_RANGE), 16.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new CrazedServantAttackGoal());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isAlive()) {
            if (++this.particleTicks % 5 == 0) {
                ServerLevel server = (ServerLevel) this.level();
                double x = this.getX() + (this.random.nextDouble() - 0.5) * 0.6;
                double y = this.getY() + this.random.nextDouble() * 1.8;
                double z = this.getZ() + (this.random.nextDouble() - 0.5) * 0.6;
                server.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0, 0, 0, 0.01);
                if (this.random.nextInt(3) == 0) {
                    server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ANVIL.defaultBlockState()),
                            x, y, z, 1, 0, 0, 0, 0.02);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            amount *= 2.0F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity living) {
            living.invulnerableTime = 0;

            float prevHealth = living.getHealth();
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            living.hurt(this.damageSources().mobAttack(this), damage);

            float dealt = prevHealth - living.getHealth();
            if (dealt > 0 && dealt < 1.0F && living.isAlive()) {
                living.invulnerableTime = 0;
                living.hurt(this.damageSources().magic(), 1.0F - dealt);
            }

            living.invulnerableTime = 0;
            this.hurt(this.damageSources().generic(), 1.0F);
            return true;
        }
        return super.doHurtTarget(target);
    }

    class CrazedServantAttackGoal extends Goal {
        public CrazedServantAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = CrazedIllagerServant.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            CrazedIllagerServant.this.setAggressive(true);
        }

        @Override
        public void stop() {
            CrazedIllagerServant.this.setAggressive(false);
            CrazedIllagerServant.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = CrazedIllagerServant.this.getTarget();
            if (target == null) return;

            CrazedIllagerServant.this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            double reach = CrazedIllagerServant.this.getBbWidth() * 2.0F + target.getBbWidth();
            if (CrazedIllagerServant.this.distanceToSqr(target) <= reach * reach) {
                CrazedIllagerServant.this.swing(InteractionHand.MAIN_HAND);
                CrazedIllagerServant.this.doHurtTarget(target);
            } else {
                CrazedIllagerServant.this.getNavigation().moveTo(target, 1.0);
            }
        }
    }
}
