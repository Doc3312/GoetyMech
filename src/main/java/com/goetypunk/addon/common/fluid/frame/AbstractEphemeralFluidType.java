package com.goetypunk.addon.common.fluid.frame;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public abstract class AbstractEphemeralFluidType extends FluidType {

    public AbstractEphemeralFluidType() {
        super(Properties.create()
                .viscosity(1000)
                .density(1000)
                .temperature(300)
                .lightLevel(4)
                .canSwim(true)
                .canDrown(true)
                .canExtinguish(true));
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            private static final ResourceLocation WATER_STILL =
                    new ResourceLocation("block/water_still");
            private static final ResourceLocation WATER_FLOW =
                    new ResourceLocation("block/water_flow");

            @Override
            public ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public int getTintColor() {
                return getFluidTintColor();
            }
        });
    }

    protected abstract int getFluidTintColor();
}
