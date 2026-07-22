package com.goetymech.addon.common.item;

import com.Polarice3.Goety.common.items.magic.FocusPack;
import com.goetymech.addon.GoetyMechMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 凝神聚晶袋。打开 GUI 时触发全局时停。
 */
public class NeuroStasisFocusPouch extends FocusPack {

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item." + GoetyMechMod.MOD_ID + ".neuro_stasis_focus_pouch");
    }
}
