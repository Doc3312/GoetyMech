package com.goetymech.addon.common.item;

import com.goetymech.addon.common.magic.spell.SculkBindSpell;
import com.Polarice3.Goety.common.items.magic.MagicFocus;

/**
 * 骞藉尶鏉熺細鑱氭櫠
 * 澶嶅悎鍨嬫硶鏈細閿佸畾鐩爣 鈫?鍙敜灏栧埡 鈫?澹版尝鐗规晥 鈫?鍙敜Warden
 */
public class SculkBindFocus extends MagicFocus {

    public SculkBindFocus() {
        super(new SculkBindSpell());
    }
}