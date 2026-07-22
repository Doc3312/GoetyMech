package com.goetymech.addon.compat.jei;

import com.goetymech.addon.GoetyMechMod;
import mezz.jei.api.recipe.RecipeType;

/**
 * JEI 配方类型注册。
 */
public class GoetyMechRecipeTypes {

    public static final RecipeType<MimicCombinationRecipe> MIMIC_COMBINATION =
            RecipeType.create(GoetyMechMod.MOD_ID, "mimic_combination", MimicCombinationRecipe.class);

    public static final RecipeType<MimicDeconstructionRecipe> MIMIC_DECONSTRUCTION =
            RecipeType.create(GoetyMechMod.MOD_ID, "mimic_deconstruction", MimicDeconstructionRecipe.class);

    private GoetyMechRecipeTypes() {}
}
