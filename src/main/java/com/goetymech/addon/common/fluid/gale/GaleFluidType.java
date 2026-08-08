package com.goetymech.addon.common.fluid.gale;

import com.goetymech.addon.common.fluid.frame.AbstractEphemeralFluidType;

/**
 * 烈风之液类型。
 */
public class GaleFluidType extends AbstractEphemeralFluidType {

    private static final int TINT_COLOR = 0xFFB89745;

    @Override
    protected int getFluidTintColor() {
        return TINT_COLOR;
    }
}
