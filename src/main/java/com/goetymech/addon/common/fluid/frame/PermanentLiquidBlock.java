package com.goetymech.addon.common.fluid.frame;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * 永久流体方块。不会计时消失，浸泡其中的生物持续获得效果。
 */
public class PermanentLiquidBlock extends LiquidBlock {

    private final List<MobEffectInstance> contactEffects;
    private final ParticleOptions particle;
    private final float particleProbability;

    public PermanentLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties,
                                List<MobEffectInstance> contactEffects,
                                ParticleOptions particle, float particleProbability) {
        super(fluid, properties);
        this.contactEffects = contactEffects;
        this.particle = particle;
        this.particleProbability = particleProbability;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (particle != null
                && state.getValue(LEVEL) == 0
                && random.nextFloat() < particleProbability) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(particle, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide
                && entity instanceof LivingEntity living
                && level.getGameTime() % 20 == 0
                && !contactEffects.isEmpty()) {
            for (MobEffectInstance effect : contactEffects) {
                living.addEffect(new MobEffectInstance(effect));
            }
        }
    }
}
