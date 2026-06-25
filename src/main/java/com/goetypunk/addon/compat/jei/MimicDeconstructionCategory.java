package com.goetypunk.addon.compat.jei;

import com.goetypunk.addon.init.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class MimicDeconstructionCategory extends AbstractRecipeCategory<MimicDeconstructionRecipe> {

    private final IDrawable background;

    public MimicDeconstructionCategory(IGuiHelper guiHelper) {
        super(
                GoetyPunkRecipeTypes.MIMIC_DECONSTRUCTION,
                Component.translatable("jei.goetypunk.category.mimic_deconstruction"),
                guiHelper.createDrawableItemStack(new ItemStack(ModItems.PROTOTYPE_MIMIC.get())),
                120, 60
        );
        this.background = guiHelper.createBlankDrawable(120, 60);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MimicDeconstructionRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 3)
                .setStandardSlotBackground()
                .addItemStack(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 66, 22)
                .setOutputSlotBackground()
                .addItemStack(recipe.getOutput());
    }
}
