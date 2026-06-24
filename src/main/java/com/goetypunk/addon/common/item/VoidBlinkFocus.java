package com.goetypunk.addon.common.item;

import com.goetypunk.addon.common.magic.spell.VoidBlinkSpell;
import com.Polarice3.Goety.common.items.magic.MagicFocus;

public class VoidBlinkFocus extends MagicFocus {

    public VoidBlinkFocus() {
        super(new VoidBlinkSpell());
    }
}
