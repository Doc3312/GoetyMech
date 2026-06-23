package com.goetypunk.addon.common.item.curio;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class AutoCastDummyWand extends Item implements IWand {
    private static final List<SpellType> NO_ELEMENT_TYPES = Arrays.asList(
            SpellType.NONE,
            SpellType.NECROMANCY,
            SpellType.ILL,
            SpellType.GEOMANCY,
            SpellType.WIND,
            SpellType.VOID
    );

    public AutoCastDummyWand() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.VOID;
    }

    @Override
    public List<SpellType> getSpellTypes() {
        return NO_ELEMENT_TYPES;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
