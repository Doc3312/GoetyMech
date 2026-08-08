package com.goetymech.addon.common.fluid.mystic;

import com.goetymech.addon.common.fluid.frame.AbstractEphemeralFluidType;

/**
 * 神秘之液类型。
 */
public class MysticFluidType extends AbstractEphemeralFluidType {

    private static final int TINT_COLOR = 0xFF9D4BB8;

    @Override
    protected int getFluidTintColor() {
        return TINT_COLOR;
    }
}
