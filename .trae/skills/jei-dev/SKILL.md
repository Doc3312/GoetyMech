---
name: "jei-dev"
description: "JEI 集成开发指南。涵盖 Plugin 注册、Recipe Category 实现、Recipe Transfer、Catalyst 关联、Ghost 拖拽、运行时交互。当需要添加 JEI 配方查看、机器配方兼容时调用本 skill。"
---

# JEI 集成开发指南

## Plugin 最小实现

```java
@JeiPlugin
public class GoetyPunkJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID =
            new ResourceLocation("goetypunk", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }
}
```

## RecipeType

```java
public class GoetyPunkRecipeTypes {
    public static final RecipeType<MyMachineRecipe> MY_MACHINE =
            RecipeType.create("goetypunk", "my_machine", MyMachineRecipe.class);
}
```

## Recipe Category 实现

```java
public class MyMachineCategory extends AbstractRecipeCategory<MyMachineRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public MyMachineCategory(IGuiHelper guiHelper) {
        super(
                GoetyPunkRecipeTypes.MY_MACHINE,
                Component.translatable("goetypunk.jei.category.my_machine"),
                guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MY_MACHINE.get())),
                120, 60
        );
        this.background = guiHelper.createBlankDrawable(120, 60);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MY_MACHINE.get()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MyMachineRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 20)
                .addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 20)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(MyMachineRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        // 自定义绘制
    }
}
```

## Recipe Transfer

### 基础 Transfer（适合标准 slot 容器）
```java
@Override
public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(
            MyMachineContainer.class,
            ModMenuTypes.MY_MACHINE.get(),
            GoetyPunkRecipeTypes.MY_MACHINE,
            0,     // recipeSlotStart
            9,     // recipeSlotCount
            9,     // inventorySlotStart
            36     // inventorySlotCount
    );
}
```

### 高级 Transfer（完全控制）
实现 `IRecipeTransferInfo` 处理非连续槽位，或实现 `IRecipeTransferHandler` 完全自定义。

## Recipe Catalyst

将机器/方块关联到 Recipe Type，点击方块即可查看相关配方。

```java
@Override
public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.MY_MACHINE.get()),
            GoetyPunkRecipeTypes.MY_MACHINE
    );
}
```

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
            GoetyPunkRecipeTypes.MY_MACHINE
    );
}
```

## 运行时交互

```java
@Override
public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    IRecipeManager recipeManager = jeiRuntime.getRecipeManager();
    // 隐藏 Recipe Category
    // recipeManager.hideRecipeCategory(VanillaRecipeTypes.CRAFTING);
    // 隐藏特定配方
    // recipeManager.hideRecipes(GoetyPunkRecipeTypes.MY_MACHINE, List.of(recipe));
}
```