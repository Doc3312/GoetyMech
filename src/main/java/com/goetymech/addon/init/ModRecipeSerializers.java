package com.goetymech.addon.init;

import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.recipe.FleshCasketInfuserRecipeSerializer;
import com.goetymech.addon.common.recipe.MimicCombinationRecipe;
import com.goetymech.addon.common.recipe.MimicDeconstructionRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组配方序列化器注册。
 */
public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, GoetyMechMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> MIMIC_COMBINATION =
            RECIPE_SERIALIZERS.register("mimic_combination", () -> MimicCombinationRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<?>> MIMIC_DECONSTRUCTION =
            RECIPE_SERIALIZERS.register("mimic_deconstruction", () -> MimicDeconstructionRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<?>> FLESH_CASKET_INFUSER =
            RECIPE_SERIALIZERS.register("flesh_casket_infuser", () -> FleshCasketInfuserRecipeSerializer.INSTANCE);
}
