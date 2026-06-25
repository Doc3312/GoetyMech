package com.goetypunk.addon.common.recipe;

import com.goetypunk.addon.common.item.PrototypeItem;
import com.goetypunk.addon.common.item.PrototypeMimicItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class MimicCombinationRecipe implements CraftingRecipe {

    private final ResourceLocation id;

    public MimicCombinationRecipe(ResourceLocation id) {
        this.id = id;
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
        return stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.CHEST)
                .values().stream().anyMatch(m ->
                        m.getId().toString().contains("armor"));
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
        return new ItemStack(ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("goetypunk", "prototype_mimic")));
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
        public @NotNull com.mojang.serialization.MapCodec<MimicCombinationRecipe> codec() {
            return com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst ->
                    inst.group(com.mojang.serialization.Codec.STRING.fieldOf("prototype")
                            .forGetter(r -> "goetypunk:prototype"))
                    .apply(inst, proto -> new MimicCombinationRecipe(null)));
        }

        @Override
        public MimicCombinationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new MimicCombinationRecipe(id);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MimicCombinationRecipe recipe) {
        }
    }
}
