package com.goetymech.addon.compat.jei;

import com.goetymech.addon.common.item.PrototypeMimicItem;
import com.goetymech.addon.init.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record MimicDeconstructionRecipe(List<ItemStack> chestplateCandidates) {

    public List<ItemStack> getInputs() {
        return chestplateCandidates.stream()
                .map(PrototypeMimicItem::createFrom)
                .toList();
    }

    public ItemStack getOutput() {
        return new ItemStack(ModItems.PROTOTYPE.get());
    }
}
