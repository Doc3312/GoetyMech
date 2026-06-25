package com.goetypunk.addon.common.recipe;

import com.goetypunk.addon.common.item.PrototypeMimicItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MimicDeconstructionRecipe implements CraftingRecipe {

    private final ResourceLocation id;

    public MimicDeconstructionRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean hasMimic = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof PrototypeMimicItem) {
                if (hasMimic) return false;
                hasMimic = true;
            } else {
                return false;
            }
        }
        return hasMimic;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        return new ItemStack(
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                        new ResourceLocation("goetypunk", "prototype")));
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 1;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess access) {
        return new ItemStack(
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                        new ResourceLocation("goetypunk", "prototype")));
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

    public static class Serializer implements RecipeSerializer<MimicDeconstructionRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public @NotNull MapCodec<MimicDeconstructionRecipe> codec() {
            return RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.STRING.fieldOf("prototype").forGetter(r -> "goetypunk:prototype")
            ).apply(inst, (proto) -> new MimicDeconstructionRecipe(null)));
        }

        @Override
        public MimicDeconstructionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new MimicDeconstructionRecipe(id);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MimicDeconstructionRecipe recipe) {
        }
    }
}
