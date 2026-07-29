# GUI 集成 + 运行时交互

## Ghost 拖拽

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
                        @Override public void accept(I ingredient) { /* 处理丢入 */ }
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

## 运行时交互

### onRuntimeAvailable

```java
@Override
public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    IRecipeManager recipeManager = jeiRuntime.getRecipeManager();
    // 隐藏 Recipe Category
    // recipeManager.hideRecipeCategory(VanillaRecipeTypes.CRAFTING);
    // 隐藏特定配方
    // recipeManager.hideRecipes(GoetyMechRecipeTypes.MY_MACHINE, List.of(recipe));
}
```

### IJeiRuntime 主要接口

| 方法 | 返回 | 用途 |
|---|---|---|
| `getRecipeManager()` | `IRecipeManager` | 配方查询/隐藏 |
| `getRecipesGui()` | `IRecipesGui` | 打开 JEI GUI |
| `getIngredientManager()` | `IIngredientManager` | 物品管理 |
| `getJeiHelpers()` | `IJeiHelpers` | 工具类 |
| `getRecipeTransferManager()` | `IRecipeTransferManager` | 转移管理 |

### IRecipeManager 常用操作

```java
recipeManager.createRecipeLookup(recipeType);           // 查找配方
recipeManager.createRecipeCategoryLookup();             // 查找 Category
recipeManager.createRecipeCatalystLookup(recipeType);   // 查找 Catalyst
recipeManager.hideRecipes(recipeType, recipes);         // 隐藏
recipeManager.unhideRecipes(recipeType, recipes);       // 取消隐藏
recipeManager.hideRecipeCategory(recipeType);           // 隐藏 Category
recipeManager.unhideRecipeCategory(recipeType);         // 取消隐藏
```

### 动态隐藏/显示 Ingredient

```java
IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();

// 隐藏
ingredientManager.addIngredientsAtRuntime(
    VanillaTypes.ITEM_STACK, List.of(new ItemStack(Items.BARRIER)));

// 取消隐藏
ingredientManager.removeIngredientsAtRuntime(
    VanillaTypes.ITEM_STACK, List.of(new ItemStack(Items.BARRIER)));
```

---

## IJeiHelpers 常用工具

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

## Plugin 完整骨架

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

## 配套 Lang Key

```properties
# zh_cn.json
"GoetyMech.jei.category.my_machine": "自定义机器",
"GoetyMech.jei.info.sculk_bind_focus": "幽匿束缚聚晶是复合型法术聚晶。\n锁定目标使其无法移动，随后召唤循声守卫。"
```
