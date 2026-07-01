package com.goetymech.addon.compat.jei;

import com.goetymech.addon.goetymechMod;
import mezz.jei.api.recipe.RecipeType;

public class GoetyMechRecipeTypes {

    public static final RecipeType<MimicCombinationRecipe> MIMIC_COMBINATION =
            RecipeType.create(goetymechMod.MOD_ID, "mimic_combination", MimicCombinationRecipe.class);

    public static final RecipeType<MimicDeconstructionRecipe> MIMIC_DECONSTRUCTION =
            RecipeType.create(goetymechMod.MOD_ID, "mimic_deconstruction", MimicDeconstructionRecipe.class);

    private GoetyMechRecipeTypes() {}
}
