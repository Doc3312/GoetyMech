# GUI 闆嗘垚 + 杩愯鏃朵氦浜?

## Ghost 鎷栨嫿

```java
@Override
public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGhostIngredientHandler(
            MyMachineScreen.class,
            new IGhostIngredientHandler<MyMachineScreen>() {
                @Override
                public <I> List<Target<I>> getTargetsTyped(
                        MyMachineScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
                    return List.of(new Target<I>() {
                        @Override public Rect2i getArea() { return new Rect2i(10, 20, 18, 18); }
                        @Override public void accept(I ingredient) { /* 澶勭悊涓㈠叆 */ }
                    });
                }
                @Override public void onComplete() {}
            }
    );

    // Recipe Click Area
    registration.addRecipeClickArea(
            MyMachineScreen.class,
            80, 30, 24, 17,
            GoetyMechRecipeTypes.MY_MACHINE
    );
}
```

---

## 杩愯鏃朵氦浜?

### onRuntimeAvailable

```java
@Override
public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    IRecipeManager recipeManager = jeiRuntime.getRecipeManager();
    // 闅愯棌 Recipe Category
    // recipeManager.hideRecipeCategory(VanillaRecipeTypes.CRAFTING);
    // 闅愯棌鐗瑰畾閰嶆柟
    // recipeManager.hideRecipes(GoetyMechRecipeTypes.MY_MACHINE, List.of(recipe));
}
```

### IJeiRuntime 涓昏鎺ュ彛

| 鏂规硶 | 杩斿洖 | 鐢ㄩ€?|
|---|---|---|
| `getRecipeManager()` | `IRecipeManager` | 閰嶆柟鏌ヨ/闅愯棌 |
| `getRecipesGui()` | `IRecipesGui` | 鎵撳紑 JEI GUI |
| `getIngredientManager()` | `IIngredientManager` | 鐗╁搧绠＄悊 |
| `getJeiHelpers()` | `IJeiHelpers` | 宸ュ叿绫?|
| `getRecipeTransferManager()` | `IRecipeTransferManager` | 杞Щ绠＄悊 |

### IRecipeManager 甯哥敤鎿嶄綔

```java
recipeManager.createRecipeLookup(recipeType);           // 鏌ユ壘閰嶆柟
recipeManager.createRecipeCategoryLookup();             // 鏌ユ壘 Category
recipeManager.createRecipeCatalystLookup(recipeType);   // 鏌ユ壘 Catalyst
recipeManager.hideRecipes(recipeType, recipes);         // 闅愯棌
recipeManager.unhideRecipes(recipeType, recipes);       // 鍙栨秷闅愯棌
recipeManager.hideRecipeCategory(recipeType);           // 闅愯棌 Category
recipeManager.unhideRecipeCategory(recipeType);         // 鍙栨秷闅愯棌
```

### 鍔ㄦ€侀殣钘?鏄剧ず Ingredient

```java
IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();

// 闅愯棌
ingredientManager.addIngredientsAtRuntime(
    VanillaTypes.ITEM_STACK, List.of(new ItemStack(Items.BARRIER)));

// 鍙栨秷闅愯棌
ingredientManager.removeIngredientsAtRuntime(
    VanillaTypes.ITEM_STACK, List.of(new ItemStack(Items.BARRIER)));
```

---

## IJeiHelpers 甯哥敤宸ュ叿

```java
IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

IDrawable blank = guiHelper.createBlankDrawable(width, height);

IDrawable texture = guiHelper.createDrawable(
    new ResourceLocation("GoetyMech", "textures/gui/my_machine.png"),
    0, 0, 120, 60, 120, 60);

IDrawable icon = guiHelper.createDrawableItemStack(
    new ItemStack(ModBlocks.MY_MACHINE.get()));

IDrawableAnimated arrow = guiHelper.createAnimatedRecipeArrow(200);

IDrawable slot = guiHelper.getSlotDrawable();
IDrawable outputSlot = guiHelper.getOutputSlot();
```

---

## Plugin 瀹屾暣楠ㄦ灦

```java
@JeiPlugin
public class GoetyMechJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID =
            new ResourceLocation("GoetyMech", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() { return UID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new MyMachineCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
            GoetyMechRecipeTypes.MY_MACHINE, MyMachineRecipeManager.getAllRecipes());
        registration.addIngredientInfo(
            ModItems.SCULK_BIND_FOCUS.get(),
            Component.translatable("GoetyMech.jei.info.sculk_bind_focus"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
            ModBlocks.MY_MACHINE.get(), GoetyMechRecipeTypes.MY_MACHINE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
            MyMachineContainer.class, ModMenuTypes.MY_MACHINE.get(),
            GoetyMechRecipeTypes.MY_MACHINE, 0, 9, 9, 36);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {}
}
```

## 閰嶅 Lang Key

```properties
# zh_cn.json
"GoetyMech.jei.category.my_machine": "鑷畾涔夋満鍣?,
"GoetyMech.jei.info.sculk_bind_focus": "骞藉尶鏉熺細鑱氭櫠鏄鍚堝瀷娉曟湳鑱氭櫠銆俓n閿佸畾鐩爣浣垮叾鏃犳硶绉诲姩锛岄殢鍚庡彫鍞ゅ惊澹板畧鍗€?
```
