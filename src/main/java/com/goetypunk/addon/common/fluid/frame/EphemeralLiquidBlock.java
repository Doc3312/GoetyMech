package com.goetypunk.addon.common.fluid.frame;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

public class EphemeralLiquidBlock extends LiquidBlock {

    private final EphemeralFluidConfig config;
    private final int timerDuration;

    public EphemeralLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties,
                                EphemeralFluidConfig config, int timerDuration) {
        super(fluid, properties);
        this.config = config;
        this.timerDuration = timerDuration;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getValue(LEVEL) == 0) {
            level.scheduleTick(pos, this, timerDuration);
        }
    }

    @Override
    public void scheduledTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide && state.getValue(LEVEL) == 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        super.scheduledTick(state, level, pos, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (config.getParticle() != null
                && state.getValue(LEVEL) == 0
                && random.nextFloat() < config.getParticleProbability()) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(config.getParticle(), x, y, z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide
                && entity instanceof Player player
                && level.getGameTime() % 20 == 0) {
            config.getContactEffect().accept(player);
        }
    }
}
