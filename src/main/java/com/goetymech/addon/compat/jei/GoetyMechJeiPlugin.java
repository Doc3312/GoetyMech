package com.goetymech.addon.compat.jei;

import com.Polarice3.Goety.common.crafting.CursedInfuserRecipes;
import com.Polarice3.Goety.compat.jei.JeiRecipeTypes;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.data.CondenserProductManager;
import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.common.item.PrototypeMimicItem;
import com.goetymech.addon.common.item.curio.FleshCasketItem;
import com.goetymech.addon.common.item.curio.TriggerType;
import com.goetymech.addon.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class GoetyMechJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID =
            new ResourceLocation(GoetyMechMod.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new MimicCombinationCategory(guiHelper),
                new MimicDeconstructionCategory(guiHelper),
                new CondenserCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ItemStack> allChestplates = BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(stack -> LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST)
                .filter(stack -> !PrototypeItem.isPrototypeItem(stack.getItem()))
                .toList();

        registration.addRecipes(GoetyMechRecipeTypes.MIMIC_COMBINATION,
                List.of(new MimicCombinationRecipe(allChestplates)));

        registration.addRecipes(GoetyMechRecipeTypes.MIMIC_DECONSTRUCTION,
                List.of(new MimicDeconstructionRecipe(allChestplates)));

        registration.addIngredientInfo(
                allChestplates.stream().map(PrototypeMimicItem::createFrom).toList(),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.goetymech.info.prototype_mimic")
        );

        // Flesh casket cycle display recipes (3 transformations per tier)
        var caskets = List.of(ModItems.FLESH_CASKET_1, ModItems.FLESH_CASKET_2, ModItems.FLESH_CASKET_3);
        var pairs = List.of(
                new CyclePair(TriggerType.B, TriggerType.C, "b_to_c"),
                new CyclePair(TriggerType.C, TriggerType.A, "c_to_a")
        );
        List<CursedInfuserRecipes> cycleRecipes = new ArrayList<>();
        for (var casketReg : caskets) {
            var item = casketReg.get();
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            for (var pair : pairs) {
                ItemStack input = new ItemStack(item);
                FleshCasketItem.setTriggerType(input, pair.from);
                ItemStack output = new ItemStack(item);
                FleshCasketItem.setTriggerType(output, pair.to);
                cycleRecipes.add(new CursedInfuserRecipes(
                        new ResourceLocation(GoetyMechMod.MOD_ID, path + "_" + pair.suffix),
                        "", Ingredient.of(input), output, 0, 60, false));
            }
        }
        registration.addRecipes(JeiRecipeTypes.CURSED_INFUSER, cycleRecipes);

        // Embryo info pages
        registration.addIngredientInfo(
                ModItems.TWISTED_EMBRYO.get(),
                Component.translatable("jei.goetymech.info.twisted_embryo")
        );
        registration.addIngredientInfo(
                ModItems.MATURE_EMBRYO.get(),
                Component.translatable("jei.goetymech.info.mature_embryo")
        );

        registerCondenserRecipes(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.WITCH_CONDENSER.get()),
                GoetyMechRecipeTypes.CONDENSER);
    }

    /** 巫法冷凝产出表：群系蒸馏条目 + 仪式产出条目 */
    private void registerCondenserRecipes(IRecipeRegistration registration) {
        List<CondenserRecipe> recipes = new ArrayList<>();
        for (CondenserProductManager.BiomeEntry entry : CondenserProductManager.getBiomeEntries()) {
            recipes.add(CondenserRecipe.biome(entry, biomeIcon(entry)));
        }
        CondenserProductManager.getRitualFluids().forEach((ritualId, fluid) ->
                recipes.add(CondenserRecipe.ritual(ritualId, fluid)));
        registration.addRecipes(GoetyMechRecipeTypes.CONDENSER, recipes);
    }

    /** 群系代表物品：exact biome 优先，其次 tag */
    private ItemStack biomeIcon(CondenserProductManager.BiomeEntry entry) {
        if (!entry.getBiomes().isEmpty()) {
            String id = entry.getBiomes().get(0).toString();
            if (id.equals("minecraft:deep_dark")) {
                return new ItemStack(Items.SCULK);
            }
        }
        if (!entry.getTags().isEmpty()) {
            String tag = entry.getTags().get(0).toString();
            if (tag.equals("minecraft:is_ocean")) {
                return new ItemStack(Items.SEAGRASS);
            }
            if (tag.equals("minecraft:is_nether")) {
                return new ItemStack(Items.NETHERRACK);
            }
        }
        return new ItemStack(Items.GRASS_BLOCK);
    }

    private record CyclePair(TriggerType from, TriggerType to, String suffix) {}
}
