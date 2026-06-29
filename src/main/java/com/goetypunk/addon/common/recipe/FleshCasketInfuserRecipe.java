package com.goetypunk.addon.common.recipe;

import com.Polarice3.Goety.common.crafting.CursedInfuserRecipes;
import com.goetypunk.addon.common.item.curio.FleshCasketItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FleshCasketInfuserRecipe extends CursedInfuserRecipes {
    public FleshCasketInfuserRecipe(ResourceLocation pId, String pGroup, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime, boolean grim) {
        super(pId, pGroup, pIngredient, pResult, pExperience, pCookingTime, grim);
    }

    @Override
    public ItemStack assemble(Container pInv, RegistryAccess pAccess) {
        ItemStack input = pInv.getItem(0);
        ItemStack result = this.result.copy();
        if (input.hasTag()) {
            result.setTag(input.getTag().copy());
        }
        FleshCasketItem.cycleTriggerType(result);
        return result;
    }

    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FLESH_CASKET_INFUSER.get();
    }
}
