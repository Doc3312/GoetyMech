package com.goetypunk.addon.common.recipe;

import com.goetypunk.addon.common.item.PrototypeItem;
import com.goetypunk.addon.common.item.PrototypeMimicItem;
import com.goetypunk.addon.init.ModItems;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MimicCombinationRecipe implements CraftingRecipe {

    private final ResourceLocation id;
    private final ItemStack result;

    public MimicCombinationRecipe(ResourceLocation id, ItemStack result) {
        this.id = id;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean hasPrototype = false;
        boolean hasChestplate = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof com.goetypunk.addon.common.item.PrototypeItem) {
                if (hasPrototype) return false;
                hasPrototype = true;
            } else if (isChestplate(stack)) {
                if (hasChestplate) return false;
                hasChestplate = true;
            } else {
                return false;
            }
        }
        return hasPrototype && hasChestplate;
    }

    private boolean isChestplate(ItemStack stack) {
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(stack);
        return slot == EquipmentSlot.CHEST;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        ItemStack chestplate = ItemStack.EMPTY;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof com.goetypunk.addon.common.item.PrototypeItem)) {
                chestplate = stack;
                break;
            }
        }
        if (chestplate.isEmpty()) return ItemStack.EMPTY;
        return PrototypeMimicItem.createFrom(chestplate);
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess access) {
        return result;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public @NotNull CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    public static class Serializer implements RecipeSerializer<MimicCombinationRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MimicCombinationRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonObject resultObj = GsonHelper.getAsJsonObject(json, "result");
            ItemStack result = ShapedRecipe.itemStackFromJson(resultObj);
            return new MimicCombinationRecipe(id, result);
        }

        @Override
        public MimicCombinationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ItemStack result = buf.readItem();
            return new MimicCombinationRecipe(id, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MimicCombinationRecipe recipe) {
            buf.writeItem(recipe.result);
        }
    }
}
