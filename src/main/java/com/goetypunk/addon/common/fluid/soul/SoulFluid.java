package com.goetypunk.addon.common.fluid.soul;

import java.util.function.Supplier;

import com.goetypunk.addon.common.fluid.frame.AbstractEphemeralFluid;
import com.goetypunk.addon.init.ModFluids;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidType;

public abstract class SoulFluid extends AbstractEphemeralFluid {

    @Override
    protected int getTimerDuration() {
        return 100;
    }

    @Override
    protected int getDecayRate() {
        return 2;
    }

    @Override
    public Fluid getFlowing() {
        return ModFluids.SOUL_LIQUID_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.SOUL_LIQUID_SOURCE.get();
    }

    @Override
    public Item getBucket() {
        return ModFluids.SOUL_LIQUID_BUCKET.get();
    }

    @Override
    protected Supplier<? extends LiquidBlock> getFluidBlock() {
        return ModFluids.SOUL_LIQUID_BLOCK;
    }

    @Override
    public FluidType getFluidType() {
        return ModFluids.SOUL_LIQUID_TYPE.get();
    }

    public static class Source extends SoulFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends SoulFluid {
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
