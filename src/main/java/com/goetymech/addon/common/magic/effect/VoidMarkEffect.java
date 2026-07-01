package com.goetymech.addon.common.magic.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class VoidMarkEffect extends MobEffect {

    public static final String TAG_TARGET = "goetymech.void_blink_target";
    public static final String TAG_REMAINING = "goetymech.void_blink_remaining";
    public static final String TAG_WINDOW_END = "goetymech.void_blink_window_end";

    public VoidMarkEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4400AA);
    }
}
