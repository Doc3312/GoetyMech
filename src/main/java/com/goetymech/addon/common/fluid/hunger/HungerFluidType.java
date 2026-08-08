package com.goetymech.addon.common.fluid.hunger;

import com.goetymech.addon.common.fluid.frame.AbstractEphemeralFluidType;

/**
 * 饥饿之液类型。
 */
public class HungerFluidType extends AbstractEphemeralFluidType {

    private static final int TINT_COLOR = 0xFF402626;

    @Override
    protected int getFluidTintColor() {
        return TINT_COLOR;
    }
}
