package com.goetymech.addon.compat.jei;

import com.goetymech.addon.goetymechMod;
import com.goetymech.addon.init.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MimicCombinationCategory extends AbstractRecipeCategory<MimicCombinationRecipe> {

    private final IDrawable background;

    public MimicCombinationCategory(IGuiHelper guiHelper) {
        super(
                goetymechRecipeTypes.MIMIC_COMBINATION,
                Component.translatable("jei.goetymech.category.mimic_combination"),
                guiHelper.createDrawableItemStack(new ItemStack(ModItems.PROTOTYPE.get())),
                176, 60
        );
        this.background = guiHelper.createBlankDrawable(176, 60);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MimicCombinationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 3)
                .setStandardSlotBackground()
                .addItemStack(recipe.getPrototype());

        builder.addSlot(RecipeIngredientRole.INPUT, 66, 3)
                .setStandardSlotBackground()
                .addItemStacks(recipe.chestplateCandidates());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 22)
                .setOutputSlotBackground()
                .addItemStack(recipe.getOutput());
    }
}
