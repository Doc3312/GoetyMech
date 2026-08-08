package com.goetymech.addon.common.fluid.hunger;

import java.util.function.Supplier;

import com.goetymech.addon.common.fluid.frame.AbstractEphemeralFluid;
import com.goetymech.addon.init.ModFluids;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidType;

/**
 * 饥饿之液。永久流体，浸泡获得饥饿与中毒。
 */
public abstract class HungerFluid extends AbstractEphemeralFluid {

    @Override
    public Fluid getFlowing() {
        return ModFluids.HUNGER_LIQUID_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.HUNGER_LIQUID_SOURCE.get();
    }

    @Override
    public Item getBucket() {
        return ModFluids.HUNGER_LIQUID_BUCKET.get();
    }

    @Override
    protected Supplier<? extends LiquidBlock> getFluidBlock() {
        return ModFluids.HUNGER_LIQUID_BLOCK;
    }

    @Override
    public FluidType getFluidType() {
        return ModFluids.HUNGER_LIQUID_TYPE.get();
    }

    public static class Source extends HungerFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends HungerFluid {
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
