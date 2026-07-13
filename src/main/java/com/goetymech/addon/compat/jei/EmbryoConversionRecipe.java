package com.goetymech.addon.compat.jei;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public record EmbryoConversionRecipe(List<ItemStack> inputs, ItemStack output) {}
