package com.goetypunk.addon.common.item;

import com.Polarice3.Goety.common.items.magic.FocusPack;
import com.goetypunk.addon.GoetyPunkMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TimeFocusPouch extends FocusPack {

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item." + GoetyPunkMod.MOD_ID + ".time_focus_pouch");
    }
}
