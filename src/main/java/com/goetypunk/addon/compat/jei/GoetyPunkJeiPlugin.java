package com.goetypunk.addon.compat.jei;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiPlugin
public class GoetyPunkJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID =
            new ResourceLocation(GoetyPunkMod.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new MimicCombinationCategory(guiHelper),
                new MimicDeconstructionCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ItemStack> allChestplates = BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(stack -> LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST)
                .toList();

        registration.addRecipes(GoetyPunkRecipeTypes.MIMIC_COMBINATION,
                List.of(new MimicCombinationRecipe(allChestplates)));

        registration.addRecipes(GoetyPunkRecipeTypes.MIMIC_DECONSTRUCTION,
                List.of(new MimicDeconstructionRecipe()));

        registration.addIngredientInfo(
                ModItems.PROTOTYPE_MIMIC.get(),
                Component.translatable("jei.goetypunk.info.prototype_mimic")
        );
    }
}
