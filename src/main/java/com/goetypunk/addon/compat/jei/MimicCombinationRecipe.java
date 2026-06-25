package com.goetypunk.addon.compat.jei;

import com.goetypunk.addon.init.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record MimicCombinationRecipe(List<ItemStack> chestplateCandidates) {

    public ItemStack getPrototype() {
        return new ItemStack(ModItems.PROTOTYPE.get());
    }

    public ItemStack getOutput() {
        return new ItemStack(ModItems.PROTOTYPE_MIMIC.get());
    }
}
