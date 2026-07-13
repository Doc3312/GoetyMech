package com.goetymech.addon.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import com.goetymech.addon.init.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class EmbryoConversionCategory extends AbstractRecipeCategory<EmbryoConversionRecipe> {

    private final IDrawable background;
    private final IDrawable arrow;

    public EmbryoConversionCategory(IGuiHelper guiHelper) {
        super(
                GoetyMechRecipeTypes.EMBRYO_CONVERSION,
                Component.translatable("jei.goetymech.category.embryo_conversion"),
                guiHelper.createDrawableItemStack(new net.minecraft.world.item.ItemStack(ModItems.TWISTED_EMBRYO.get())),
                120, 44
        );
        this.background = guiHelper.createBlankDrawable(120, 44);
        this.arrow = guiHelper.createDrawable(
                new ResourceLocation("jei", "textures/gui/gui_vanilla.png"), 65, 118, 22, 15);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void draw(EmbryoConversionRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 48, 15);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EmbryoConversionRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 22, 14)
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 14)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }
}
