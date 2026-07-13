package com.goetymech.addon.compat.jei;

import com.goetymech.addon.common.item.PrototypeMimicItem;
import com.goetymech.addon.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record MimicCombinationRecipe(List<ItemStack> chestplateCandidates) {

    public ItemStack getPrototype() {
        return new ItemStack(ModItems.PROTOTYPE.get());
    }

    public ItemStack getOutput() {
        return PrototypeMimicItem.createFrom(new ItemStack(Items.IRON_CHESTPLATE));
    }
}
