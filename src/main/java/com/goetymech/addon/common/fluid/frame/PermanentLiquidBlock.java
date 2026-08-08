package com.goetymech.addon.common.fluid.frame;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * 永久流体方块。不会计时消失。
 * 浸泡效果由 {@link com.goetymech.addon.common.fluid.FluidContactEvents} 事件驱动。
 */
public class PermanentLiquidBlock extends LiquidBlock {

    private final ParticleOptions particle;
    private final float particleProbability;

    public PermanentLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties,
                                ParticleOptions particle, float particleProbability) {
        super(fluid, properties);
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
}
