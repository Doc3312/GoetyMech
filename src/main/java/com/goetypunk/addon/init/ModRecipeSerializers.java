package com.goetypunk.addon.init;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.recipe.FleshCasketInfuserRecipeSerializer;
import com.goetypunk.addon.common.recipe.MimicCombinationRecipe;
import com.goetypunk.addon.common.recipe.MimicDeconstructionRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, GoetyPunkMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> MIMIC_COMBINATION =
            RECIPE_SERIALIZERS.register("mimic_combination", () -> MimicCombinationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<?>> MIMIC_DECONSTRUCTION =
            RECIPE_SERIALIZERS.register("mimic_deconstruction", () -> MimicDeconstructionRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<?>> FLESH_CASKET_INFUSER =
            RECIPE_SERIALIZERS.register("flesh_casket_infuser", () -> FleshCasketInfuserRecipeSerializer.INSTANCE);
}
