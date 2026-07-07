# Mimic Recipe Not Loading

status: disputed

## Symptom
RecipeManager shows "Loaded 27 recipes", mimic recipes not among them. JSON files exist in build output. No error messages.

## Investigation
- Verified JSON at `build/resources/main/data/GoetyMech/recipes/mimic_combination.json` exists with correct structure
- `ForgeRegistries.RECIPE_SERIALIZERS` registry populated
- Serializers registered via `ModRecipeSerializers` + `GoetyMechMod`
- `ModItems.PROTOTYPE_MIMIC` is stale (`get()` returns null) at recipe parse time
- Debug log in `fromJson()` suspected to show `ModItems.PROTOTYPE_MIMIC.get()` returning null (unverified)

## Root Cause Hypothesis (disputed)
`ModItems` items not fully initialized when RecipeManager loads recipes from JSON. Recipe JSON calls `ModItems.PROTOTYPE_MIMIC.get()` which returns null because `RegistryObject` not yet resolved.

## Fix Attempted
`getResultItem()` changed from `ForgeRegistries.ITEMS.getValue()` to `ModItems.PROTOTYPE_MIMIC.get()` — moved from `fromJson()` to `getResultItem()`. Same null issue because `RegistryObject` depends on deferred register resolution order.

## Resolution
Root cause still unknown. Workaround needed:
- Consider fabricating result ItemStack from registry name directly (bypass RegistryObject)
- Consider using `ForgeRegistries.ITEMS.getValue()` with `PROTOTYPE_MIMIC.getId()` (RegistryObject may not be available at recipe parse time)

## Related Files
- `MimicCombinationRecipe.java`
- `MimicDeconstructionRecipe.java`
- `ModRecipeSerializers.java`
- `ModItems.java`

## Session Context
2026-06-24: Two consecutive debugging sessions (~30 min total). Recipe loading failed in both runs (with and without recipe build output log evidence).
