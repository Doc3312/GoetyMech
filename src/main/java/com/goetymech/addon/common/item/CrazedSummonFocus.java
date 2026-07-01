package com.goetymech.addon.common.item;

import com.goetymech.addon.common.magic.spell.CrazedSummonSpell;
import com.Polarice3.Goety.common.items.magic.MagicFocus;

public class CrazedSummonFocus extends MagicFocus {
    public CrazedSummonFocus() {
        super(new CrazedSummonSpell());
    }
}
