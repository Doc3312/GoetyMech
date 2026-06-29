package com.goetypunk.addon.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class FleshCasketInfuserRecipeSerializer implements RecipeSerializer<FleshCasketInfuserRecipe> {
    public static final FleshCasketInfuserRecipeSerializer INSTANCE = new FleshCasketInfuserRecipeSerializer();

    private FleshCasketInfuserRecipeSerializer() {}

    public FleshCasketInfuserRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
        String s = GsonHelper.getAsString(pJson, "group", "");
        JsonElement jsonelement = GsonHelper.isArrayNode(pJson, "ingredient")
                ? GsonHelper.getAsJsonArray(pJson, "ingredient")
                : GsonHelper.getAsJsonObject(pJson, "ingredient");
        Ingredient ingredient = Ingredient.fromJson(jsonelement);
        boolean grim = false;
        if (pJson.has("grim")){
            grim = GsonHelper.getAsBoolean(pJson, "grim");
        }
        if (!pJson.has("result")) {
            throw new com.google.gson.JsonSyntaxException("Missing result, expected to find a string or object");
        }
        ItemStack itemstack;
        if (pJson.get("result").isJsonObject()) {
            itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
        } else {
            String s1 = GsonHelper.getAsString(pJson, "result");
            ResourceLocation resourcelocation = new ResourceLocation(s1);
            itemstack = new ItemStack(BuiltInRegistries.ITEM.getOptional(resourcelocation).orElseThrow(() -> {
                return new IllegalStateException("Item: " + s1 + " does not exist");
            }));
        }
        int i = GsonHelper.getAsInt(pJson, "cookingTime", 60);
        return new FleshCasketInfuserRecipe(pRecipeId, s, ingredient, itemstack, 0.0F, i, grim);
    }

    public FleshCasketInfuserRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
        String s = pBuffer.readUtf(32767);
        Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
        ItemStack itemstack = pBuffer.readItem();
        int i = pBuffer.readVarInt();
        boolean grim = pBuffer.readBoolean();
        return new FleshCasketInfuserRecipe(pRecipeId, s, ingredient, itemstack, 0.0F, i, grim);
    }

    public void toNetwork(FriendlyByteBuf pBuffer, FleshCasketInfuserRecipe pRecipe) {
        pBuffer.writeUtf(pRecipe.group);
        pRecipe.ingredient.toNetwork(pBuffer);
        pBuffer.writeItem(pRecipe.result);
        pBuffer.writeVarInt(pRecipe.cookingTime);
        pBuffer.writeBoolean(pRecipe.grim);
    }
}
