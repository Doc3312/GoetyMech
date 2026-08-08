package com.goetymech.addon.common.fluid.mystic;

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
 * 神秘之液。永久流体，浸泡无效果，饮用随机获得四种气场之一。
 */
public abstract class MysticFluid extends AbstractEphemeralFluid {

    @Override
    public Fluid getFlowing() {
        return ModFluids.MYSTIC_LIQUID_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.MYSTIC_LIQUID_SOURCE.get();
    }

    @Override
    public Item getBucket() {
        return ModFluids.MYSTIC_LIQUID_BUCKET.get();
    }

    @Override
    protected Supplier<? extends LiquidBlock> getFluidBlock() {
        return ModFluids.MYSTIC_LIQUID_BLOCK;
    }

    @Override
    public FluidType getFluidType() {
        return ModFluids.MYSTIC_LIQUID_TYPE.get();
    }

    public static class Source extends MysticFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends MysticFluid {
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
