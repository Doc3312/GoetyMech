package com.goetymech.addon.common.item;

import com.goetymech.addon.common.magic.spell.GatewaySpell;
import com.Polarice3.Goety.common.items.magic.MagicFocus;

public class GatewayFocus extends MagicFocus {

    public GatewayFocus() {
        super(new GatewaySpell());
    }
}
