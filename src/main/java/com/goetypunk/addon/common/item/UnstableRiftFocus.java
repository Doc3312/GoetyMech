package com.goetypunk.addon.common.item;

import com.goetypunk.addon.common.magic.spell.UnstableRiftSpell;
import com.Polarice3.Goety.common.items.magic.MagicFocus;

public class UnstableRiftFocus extends MagicFocus {
    public UnstableRiftFocus() {
        super(new UnstableRiftSpell());
    }
}
