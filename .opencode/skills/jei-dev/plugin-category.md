# Plugin + Recipe Category + Recipes

## Plugin 最小实现

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

## Recipe Category 实现

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

### IRecipeSlotBuilder 常用方法

| 方法 | 说明 |
|---|---|
| `setStandardSlotBackground()` | 标准 18x18 输入槽 |
| `setOutputSlotBackground()` | 26x26 输出槽 |
| `setBackground(IDrawable, x, y)` | 自定义背景 |
| `setOverlay(IDrawable, x, y)` | 叠加绘制 |
| `addItemStacks(List<ItemStack>)` | 多候选（循环显示） |
| `addItemStack(ItemStack)` | 单物品 |
| `addFluidStack(Fluid, long)` | 流体 |
| `setCustomRenderer(IIngredientType, IIngredientRenderer)` | 自定义渲染 |
| `addRichTooltipCallback(...)` | 自定义 tooltip |
| `setSlotName(String)` | 命名槽位 |

### 注册 Category

```java
@Override
public void registerCategories(IRecipeCategoryRegistration registration) {
    IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
    registration.addRecipeCategories(new MyMachineCategory(guiHelper));
}
```

## 注册 Recipes

```java
@Override
public void registerRecipes(IRecipeRegistration registration) {
    // 自定义配方
    List<MyMachineRecipe> recipes = MyMachineRecipeManager.getAllRecipes();
    registration.addRecipes(GoetyMechRecipeTypes.MY_MACHINE, recipes);

    // 物品说明信息页
    registration.addIngredientInfo(
            ModItems.SCULK_BIND_FOCUS.get(),
            Component.translatable("GoetyMech.jei.info.sculk_bind_focus")
    );
}
```

### addIngredientInfo 重载

```java
// 单个 ItemLike
registration.addIngredientInfo(ModBlocks.MY_BLOCK.get(), Component.literal("..."));

// 单个 ItemStack + 指定 IngredientType
registration.addIngredientInfo(
    new ItemStack(ModItems.MY_ITEM.get()), VanillaTypes.ITEM_STACK, Component.literal("..."));

// 批量
registration.addIngredientInfo(
    List.of(itemStack1, itemStack2), VanillaTypes.ITEM_STACK, Component.literal("..."));
```

Vanilla 内置支持的 Recipe 类型（无需自定义 Category）：合成台、熔炉/烟熏炉/高炉/营火、酿造、铁砧、切石机、燃料。
