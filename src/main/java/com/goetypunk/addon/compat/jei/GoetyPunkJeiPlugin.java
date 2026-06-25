package com.goetypunk.addon.compat.jei;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class GoetyPunkJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID =
            new ResourceLocation(GoetyPunkMod.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                ModItems.PROTOTYPE_MIMIC.get(),
                Component.translatable("jei.goetypunk.info.prototype_mimic")
        );
    }
}
