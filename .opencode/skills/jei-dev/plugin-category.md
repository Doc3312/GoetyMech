# Plugin + Recipe Category + Recipes

## Plugin 鏈€灏忓疄鐜?

```java
@JeiPlugin
public class GoetyMechJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID =
            new ResourceLocation("GoetyMech", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }
}
```

## RecipeType

```java
public class GoetyMechRecipeTypes {
    public static final RecipeType<MyMachineRecipe> MY_MACHINE =
            RecipeType.create("GoetyMech", "my_machine", MyMachineRecipe.class);

    private GoetyMechRecipeTypes() {}
}
```

## Recipe Category 瀹炵幇

```java
public class MyMachineCategory extends AbstractRecipeCategory<MyMachineRecipe> {

    private final IDrawable background;
    private final IDrawable icon;

    public MyMachineCategory(IGuiHelper guiHelper) {
        super(
                GoetyMechRecipeTypes.MY_MACHINE,
                Component.translatable("GoetyMech.jei.category.my_machine"),
                guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MY_MACHINE.get())),
                120, 60
        );
        this.background = guiHelper.createBlankDrawable(120, 60);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MY_MACHINE.get()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MyMachineRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 20)
                .setStandardSlotBackground()
                .addItemStacks(recipe.getInputs());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 20)
                .setOutputSlotBackground()
                .addItemStack(recipe.getOutput());
    }
}
```

### IRecipeSlotBuilder 甯哥敤鏂规硶

| 鏂规硶 | 璇存槑 |
|---|---|
| `setStandardSlotBackground()` | 鏍囧噯 18x18 杈撳叆妲?|
| `setOutputSlotBackground()` | 26x26 杈撳嚭妲?|
| `setBackground(IDrawable, x, y)` | 鑷畾涔夎儗鏅?|
| `setOverlay(IDrawable, x, y)` | 鍙犲姞缁樺埗 |
| `addItemStacks(List<ItemStack>)` | 澶氬€欓€夛紙寰幆鏄剧ず锛?|
| `addItemStack(ItemStack)` | 鍗曠墿鍝?|
| `addFluidStack(Fluid, long)` | 娴佷綋 |
| `setCustomRenderer(IIngredientType, IIngredientRenderer)` | 鑷畾涔夋覆鏌?|
| `addRichTooltipCallback(...)` | 鑷畾涔?tooltip |
| `setSlotName(String)` | 鍛藉悕妲戒綅 |

### 娉ㄥ唽 Category

```java
@Override
public void registerCategories(IRecipeCategoryRegistration registration) {
    IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
    registration.addRecipeCategories(new MyMachineCategory(guiHelper));
}
```

## 娉ㄥ唽 Recipes

```java
@Override
public void registerRecipes(IRecipeRegistration registration) {
    // 鑷畾涔夐厤鏂?
    List<MyMachineRecipe> recipes = MyMachineRecipeManager.getAllRecipes();
    registration.addRecipes(GoetyMechRecipeTypes.MY_MACHINE, recipes);

    // 鐗╁搧璇存槑淇℃伅椤?
    registration.addIngredientInfo(
            ModItems.SCULK_BIND_FOCUS.get(),
            Component.translatable("GoetyMech.jei.info.sculk_bind_focus")
    );
}
```

### addIngredientInfo 閲嶈浇

```java
// 鍗曚釜 ItemLike
registration.addIngredientInfo(ModBlocks.MY_BLOCK.get(), Component.literal("..."));

// 鍗曚釜 ItemStack + 鎸囧畾 IngredientType
registration.addIngredientInfo(
    new ItemStack(ModItems.MY_ITEM.get()), VanillaTypes.ITEM_STACK, Component.literal("..."));

// 鎵归噺
registration.addIngredientInfo(
    List.of(itemStack1, itemStack2), VanillaTypes.ITEM_STACK, Component.literal("..."));
```

Vanilla 鍐呯疆鏀寔鐨?Recipe 绫诲瀷锛堟棤闇€鑷畾涔?Category锛夛細鍚堟垚鍙般€佺啍鐐?鐑熺啅鐐?楂樼倝/钀ョ伀銆侀吙閫犮€侀搧鐮с€佸垏鐭虫満銆佺噧鏂欍€?
