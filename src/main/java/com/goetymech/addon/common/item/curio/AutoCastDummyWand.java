package com.goetymech.addon.common.item.curio;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoCastDummyWand extends Item implements IWand {
    private final SpellType primaryType;
    private final List<SpellType> spellTypes;

    public AutoCastDummyWand() {
        this(SpellType.VOID, SpellType.NONE, SpellType.NECROMANCY,
                SpellType.ILL, SpellType.GEOMANCY, SpellType.WIND);
    }

    public AutoCastDummyWand(SpellType primary, SpellType... additional) {
        super(new Properties().stacksTo(1));
        this.primaryType = primary;
        List<SpellType> list = new ArrayList<>();
        list.add(primary);
        Collections.addAll(list, additional);
        this.spellTypes = Collections.unmodifiableList(list);
    }

    @Override
    public SpellType getSpellType() {
        return primaryType;
    }

    @Override
    public List<SpellType> getSpellTypes() {
        return spellTypes;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new SoulUsingItemHandler(stack);
    }
}
