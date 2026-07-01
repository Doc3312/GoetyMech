package com.goetymech.addon.compat.jei;

import net.minecraft.world.item.ItemStack;

public record MimicDeconstructionRecipe() {

    public ItemStack getInput() {
        return new ItemStack(com.goetymech.addon.init.ModItems.PROTOTYPE_MIMIC.get());
    }

    public ItemStack getOutput() {
        return new ItemStack(com.goetymech.addon.init.ModItems.PROTOTYPE.get());
    }
}
