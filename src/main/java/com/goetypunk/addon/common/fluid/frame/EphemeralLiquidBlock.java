package com.goetypunk.addon.common.fluid.frame;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
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
    private final int decayRate;

    public EphemeralLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties,
                                EphemeralFluidConfig config, int timerDuration, int decayRate) {
        super(fluid, properties);
        this.config = config;
        this.timerDuration = timerDuration;
        this.decayRate = decayRate;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide && state.getValue(LEVEL) == 0) {
            level.scheduleTick(pos, this, timerDuration);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int blockLevel = state.getValue(LEVEL);

        if (blockLevel == 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            scheduleNeighborFlowing(level, pos);
            return;
        }

        if (!hasSourceNeighbor(level, pos)) {
            int newLevel = blockLevel + decayRate;
            if (newLevel > 7) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            } else {
                level.setBlock(pos, state.setValue(LEVEL, newLevel), 3);
                level.scheduleTick(pos, this, 1);
            }
            return;
        }

        super.tick(state, level, pos, random);
    }

    private void scheduleNeighborFlowing(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.is(this) && neighborState.getValue(LEVEL) > 0) {
                boolean hasOtherSource = false;
                for (Direction checkDir : Direction.values()) {
                    BlockPos checkPos = neighbor.relative(checkDir);
                    if (!checkPos.equals(pos)) {
                        BlockState checkState = level.getBlockState(checkPos);
                        if (checkState.is(this) && checkState.getValue(LEVEL) == 0) {
                            hasOtherSource = true;
                            break;
                        }
                    }
                }
                if (!hasOtherSource) {
                    level.scheduleTick(neighbor, this, 1);
                }
            }
        }
    }

    private boolean hasSourceNeighbor(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.is(this) && neighborState.getValue(LEVEL) == 0) {
                return true;
            }
        }
        return false;
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
