package com.goetymech.addon.init;

import com.Polarice3.Goety.api.ritual.RitualType;
import com.goetymech.addon.common.ritual.type.MechanicalRitualType;

/**
 * 模组仪式类型注册。
 */
public class ModRitualTypes {
    public static void register() {
        RitualType.addRitualType("mechanical", new MechanicalRitualType());
    }
}
