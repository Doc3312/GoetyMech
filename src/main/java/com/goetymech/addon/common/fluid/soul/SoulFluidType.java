package com.goetymech.addon.common.fluid.soul;

import com.goetymech.addon.common.fluid.frame.AbstractEphemeralFluidType;

/**
 * 灵魂液类型。
 */
public class SoulFluidType extends AbstractEphemeralFluidType {

    private static final int TINT_COLOR = 0xCC7CFCFF;

    @Override
    protected int getFluidTintColor() {
        return TINT_COLOR;
    }
}
