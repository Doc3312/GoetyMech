package com.goetymech.addon.compat.jei;

import com.goetymech.addon.common.item.PrototypeMimicItem;
import com.goetymech.addon.init.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 拟态分解 JEI 分类。
 */
public class MimicDeconstructionCategory extends AbstractRecipeCategory<MimicDeconstructionRecipe> {

    private final IDrawable background;

    public MimicDeconstructionCategory(IGuiHelper guiHelper) {
        super(
                GoetyMechRecipeTypes.MIMIC_DECONSTRUCTION,
                Component.translatable("jei.goetymech.category.mimic_deconstruction"),
                guiHelper.createDrawableItemStack(PrototypeMimicItem.createFrom(new ItemStack(Items.IRON_CHESTPLATE))),
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
                .addItemStacks(recipe.getInputs());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 66, 22)
                .setOutputSlotBackground()
                .addItemStack(recipe.getOutput());
    }
}
