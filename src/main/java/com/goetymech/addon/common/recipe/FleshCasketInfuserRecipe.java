package com.goetymech.addon.common.recipe;

import com.Polarice3.Goety.common.crafting.CursedInfuserRecipes;
import com.goetymech.addon.common.item.curio.FleshCasketItem;
import com.goetymech.addon.init.ModRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.items.ItemStackHandler;

public class FleshCasketInfuserRecipe extends CursedInfuserRecipes {
    public FleshCasketInfuserRecipe(ResourceLocation pId, String pGroup, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime, boolean grim) {
        super(pId, pGroup, pIngredient, pResult, pExperience, pCookingTime, grim);
    }

    @Override
    public ItemStack assemble(Container pInv, RegistryAccess pAccess) {
        ItemStack input = pInv.getItem(0);
        ItemStack result = this.result.copy();
        CompoundTag tag = input.hasTag() ? input.getTag().copy() : new CompoundTag();
        result.setTag(tag);
        var inputCap = input.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).resolve();
        var resultCap = result.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).resolve();
        if (inputCap.isPresent() && resultCap.isPresent()
                && inputCap.get() instanceof ItemStackHandler ih
                && resultCap.get() instanceof ItemStackHandler rh) {
            for (int i = 0; i < Math.min(ih.getSlots(), rh.getSlots()); i++) {
                rh.setStackInSlot(i, ih.getStackInSlot(i).copy());
            }
        }
        FleshCasketItem.cycleTriggerType(result);
        return result;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pAccess) {
        ItemStack stack = this.result.copy();
        FleshCasketItem.cycleTriggerType(stack);
        return stack;
    }

    public String getGroup() { return this.group; }

    public Ingredient getIngredient() { return this.ingredient; }

    public ItemStack getResult() { return this.result; }

    public int getCookingTime() { return this.cookingTime; }

    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FLESH_CASKET_INFUSER.get();
    }
}
