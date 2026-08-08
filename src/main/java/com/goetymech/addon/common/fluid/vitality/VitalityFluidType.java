package com.goetymech.addon.common.fluid.vitality;

import com.goetymech.addon.common.fluid.frame.AbstractEphemeralFluidType;

/**
 * 活力之液类型。
 */
public class VitalityFluidType extends AbstractEphemeralFluidType {

    private static final int TINT_COLOR = 0xFFC1C1C1;

    @Override
    protected int getFluidTintColor() {
        return TINT_COLOR;
    }
}
