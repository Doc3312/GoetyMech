package com.goetymech.addon.compat.jei;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.init.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 巫法冷凝 JEI 类别。单类别双布局：
 *  - BIOME：群系蒸馏条目（代表物品槽 + 群系名 + 各液体概率）
 *  - RITUAL：仪式产出条目（仪式名 + 液体输出）
 */
public class CondenserCategory extends AbstractRecipeCategory<CondenserRecipe> {

    private static final int BIOME_ICON_X = 15;
    private static final int BIOME_ICON_Y = 22;
    private static final int FLUID_X = 70;
    private static final int FLUID_Y = 12;

    private final IDrawable background;

    public CondenserCategory(IGuiHelper guiHelper) {
        super(
                GoetyMechRecipeTypes.CONDENSER,
                Component.translatable("jei.goetymech.category.condenser"),
                guiHelper.createDrawableItemStack(new ItemStack(ModItems.WITCH_CONDENSER.get())),
                176, 76
        );
        this.background = guiHelper.createBlankDrawable(176, 76);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CondenserRecipe recipe, IFocusGroup focuses) {
        if (recipe.getKind() == CondenserRecipe.Kind.BIOME) {
            builder.addSlot(RecipeIngredientRole.INPUT, BIOME_ICON_X, BIOME_ICON_Y)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.getIcon());
            addFluidSlots(builder, recipe, false);
        } else {
            addFluidSlots(builder, recipe, true);
        }
    }

    private void addFluidSlots(IRecipeLayoutBuilder builder, CondenserRecipe recipe, boolean ritual) {
        List<FluidStack> fluids = recipe.getFluids();
        int rows = (fluids.size() + 2) / 3;
        int y = FLUID_Y + (ritual ? 8 : 0);
        for (int i = 0; i < fluids.size(); i++) {
            int col = i % 3;
            int row = i / 3;
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT,
                            FLUID_X + col * 22, y + row * 24)
                    .setStandardSlotBackground()
                    .addFluidStack(fluids.get(i).getFluid(), fluids.get(i).getAmount());
            if (ritual) {
                slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.goetymech.category.condenser.ritual.amount")
                                .withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    @Override
    public void draw(CondenserRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        if (recipe.getKind() == CondenserRecipe.Kind.BIOME) {
            guiGraphics.drawString(font, biomeLabel(recipe), BIOME_ICON_X - 10, 8, 0xFFFFFF);
            List<FluidStack> fluids = recipe.getFluids();
            for (int i = 0; i < fluids.size(); i++) {
                int col = i % 3;
                int row = i / 3;
                String chance = Math.round(recipe.getChances().get(i)) + "%";
                guiGraphics.drawString(font, chance, FLUID_X + col * 22 + 4, FLUID_Y + row * 24 + 20, 0xFFE0A0);
            }
        } else {
            guiGraphics.drawString(font, Component.literal(recipe.getRitualName()),
                    10, 10, 0xFFFFFF);
        }
    }

    /** 群系名：biomes 用本地化名（如「深暗之域」），tags 用 #id 形式 */
    private Component biomeLabel(CondenserRecipe recipe) {
        if (!recipe.getBiomes().isEmpty()) {
            return Component.translatable(ResourceKey.create(Registries.BIOME, recipe.getBiomes().get(0))
                    .location().toLanguageKey());
        }
        if (!recipe.getTags().isEmpty()) {
            return Component.literal("#" + recipe.getTags().get(0));
        }
        return Component.empty();
    }
}
