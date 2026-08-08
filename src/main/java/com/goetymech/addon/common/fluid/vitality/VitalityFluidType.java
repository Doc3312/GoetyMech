package com.goetymech.addon.common.fluid.vitality;

import com.goetymech.addon.common.fluid.frame.AbstractEphemeralFluidType;

/**
 * 活力之液类型。
 */
public class VitalityFluidType extends AbstractEphemeralFluidType {

    private static final int TINT_COLOR = 0xFF636363;

    @Override
    protected int getFluidTintColor() {
        return TINT_COLOR;
    }
}
