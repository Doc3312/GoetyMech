package com.goetypunk.addon.common.entity;

import com.Polarice3.Goety.common.entities.ally.illager.VindicatorServant;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

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
        this.goalSelector.addGoal(4, new CrazedServantAttackGoal());
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
        if (source.isFire()) {
            amount *= 2.0F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.invulnerableTime = 0;
            this.hurt(this.damageSources().generic(), 1.0F);
        }
        return hurt;
    }

    class CrazedServantAttackGoal extends MeleeAttackGoal {
        public CrazedServantAttackGoal() {
            super(CrazedIllagerServant.this, 1.0, true, 5);
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return (this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F) + target.getBbWidth();
        }
    }
}
