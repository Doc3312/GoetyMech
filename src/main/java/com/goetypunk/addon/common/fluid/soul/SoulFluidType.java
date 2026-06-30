package com.goetypunk.addon.common.fluid.soul;

import com.goetypunk.addon.common.fluid.frame.AbstractEphemeralFluidType;

public class SoulFluidType extends AbstractEphemeralFluidType {

    private static final int TINT_COLOR = 0xCC7CFCFF;

    @Override
    protected int getFluidTintColor() {
        return TINT_COLOR;
    }
}
