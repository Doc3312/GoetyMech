package com.goetypunk.addon.compat.jei;

import com.goetypunk.addon.GoetyPunkMod;
import mezz.jei.api.recipe.RecipeType;

public class GoetyPunkRecipeTypes {

    public static final RecipeType<MimicCombinationRecipe> MIMIC_COMBINATION =
            RecipeType.create(GoetyPunkMod.MOD_ID, "mimic_combination", MimicCombinationRecipe.class);

    public static final RecipeType<MimicDeconstructionRecipe> MIMIC_DECONSTRUCTION =
            RecipeType.create(GoetyPunkMod.MOD_ID, "mimic_deconstruction", MimicDeconstructionRecipe.class);

    private GoetyPunkRecipeTypes() {}
}
