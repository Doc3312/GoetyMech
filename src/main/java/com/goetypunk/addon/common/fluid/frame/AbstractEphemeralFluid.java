package com.goetypunk.addon.common.fluid.frame;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidType;

public abstract class AbstractEphemeralFluid extends FlowingFluid {

    protected abstract int getTimerDuration();
    protected abstract int getDecayRate();
    protected abstract Supplier<? extends LiquidBlock> getFluidBlock();
    protected abstract FluidType getFluidType();

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getSource() || fluid == getFlowing();
    }

    @Override
    public int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    public int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return getFluidBlock().get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn, Direction direction) {
        return direction == Direction.DOWN && !isSame(fluidIn);
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
    }

    @Override
    protected int getSpreadDelay(Level level, BlockPos pos, FluidState existing, FluidState newFluid) {
        return getTickDelay(level);
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    @Override
    public void scheduledTick(Level level, BlockPos pos, FluidState state) {
        if (level.isClientSide) {
            super.scheduledTick(level, pos, state);
            return;
        }

        if (!state.isSource()) {
            if (!sourceExistsNearby(level, pos)) {
                int currentLevel = getAmount(state);
                int newLevel = Math.max(0, currentLevel - getDecayRate());
                if (newLevel <= 0) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                } else {
                    FluidState newState = getFlowing(newLevel, state);
                    level.setBlock(pos, newState.createLegacyBlock(), 3);
                    Block block = level.getBlockState(pos).getBlock();
                    level.scheduleTick(pos, block, 1);
                }
                return;
            }
        }

        super.scheduledTick(level, pos, state);
    }

    private boolean sourceExistsNearby(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            FluidState neighbor = level.getFluidState(pos.relative(dir));
            if (neighbor.getType() instanceof AbstractEphemeralFluid && neighbor.isSource()) {
                return true;
            }
        }
        return false;
    }

    public static abstract class Source extends AbstractEphemeralFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static abstract class Flowing extends AbstractEphemeralFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
