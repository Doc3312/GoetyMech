package com.goetypunk.addon.common.item;

import com.goetypunk.addon.common.magic.spell.SculkBindSpell;
import com.Polarice3.Goety.common.items.magic.MagicFocus;

/**
 * 幽匿束缚聚晶
 * 复合型法术：锁定目标 → 召唤尖刺 → 声波特效 → 召唤Warden
 */
public class SculkBindFocus extends MagicFocus {

    public SculkBindFocus() {
        super(new SculkBindSpell());
    }
}