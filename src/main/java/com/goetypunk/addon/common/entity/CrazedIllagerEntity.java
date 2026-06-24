package com.goetypunk.addon.common.entity;

import com.goetypunk.addon.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class CrazedIllagerEntity extends AbstractIllager {

    private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE =
            difficulty -> difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD;

    private boolean isJohnny;
    private int particleTicks;

    public CrazedIllagerEntity(EntityType<? extends AbstractIllager> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.525)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CrazedBreakDoorGoal(this));
        this.goalSelector.addGoal(2, new RaiderOpenDoorGoal(this));
        this.goalSelector.addGoal(3, new HoldGroundAttackGoal(this, 10.0F));
        this.goalSelector.addGoal(4, new CrazedAttackGoal());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
        if (this.isJohnny) {
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, true, false,
                    entity -> entity != this && !(entity instanceof Raider)));
        }
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
            this.invulnerableTime = 0;
            return true;
        }
        return super.doHurtTarget(target);
    }

    @Override
    public void customServerAiStep() {
        if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this)) {
            boolean canBreakDoors = this.level().getDifficulty() == Difficulty.NORMAL
                    || this.level().getDifficulty() == Difficulty.HARD;
            ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(canBreakDoors);
        }
        super.customServerAiStep();
    }

    @Override
    public IllagerArmPose getArmPose() {
        if (this.isAggressive()) return IllagerArmPose.ATTACKING;
        if (this.isCelebrating()) return IllagerArmPose.CELEBRATING;
        return IllagerArmPose.CROSSED;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (super.isAlliedTo(entity)) return true;
        if (entity instanceof LivingEntity living && living.getMobType() == MobType.ILLAGER
                && this.getTeam() == null && entity.getTeam() == null) {
            return true;
        }
        return false;
    }

    @Override
    public void applyRaidBuffs(int wave, boolean leader) {
        ItemStack weapon = new ItemStack(Items.IRON_AXE);
        int enchantLevel = wave > 3 ? 2 : 1;
        if (this.random.nextFloat() <= 0.5F) {
            weapon.enchant(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS, enchantLevel);
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, weapon);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        if (!this.isJohnny && name != null && name.getString().equals("Johnny")) {
            this.isJohnny = true;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.isJohnny) tag.putBoolean("Johnny", true);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Johnny")) this.isJohnny = tag.getBoolean("Johnny");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VINDICATOR_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VINDICATOR_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VINDICATOR_HURT;
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.VINDICATOR_CELEBRATE;
    }

    class CrazedAttackGoal extends Goal {
        private int attackTimer;

        public CrazedAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = CrazedIllagerEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            CrazedIllagerEntity.this.setAggressive(true);
            this.attackTimer = 0;
        }

        @Override
        public void stop() {
            CrazedIllagerEntity.this.setAggressive(false);
            CrazedIllagerEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = CrazedIllagerEntity.this.getTarget();
            if (target == null) return;

            CrazedIllagerEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            double reach = CrazedIllagerEntity.this.getBbWidth() * 2.0F + target.getBbWidth();
            if (CrazedIllagerEntity.this.distanceToSqr(target) <= reach * reach) {
                if (--this.attackTimer <= 0) {
                    this.attackTimer = 4;
                    CrazedIllagerEntity.this.swing(InteractionHand.MAIN_HAND);
                    CrazedIllagerEntity.this.doHurtTarget(target);
                }
            } else {
                CrazedIllagerEntity.this.getNavigation().moveTo(target, 1.0);
            }
        }
    }

    static class CrazedBreakDoorGoal extends BreakDoorGoal {
        public CrazedBreakDoorGoal(Mob mob) {
            super(mob, DOOR_BREAKING_PREDICATE);
        }
    }
}
