---
name: "jei-dev"
description: "JEI 闆嗘垚寮€鍙戞寚鍗椼€傛兜鐩?Plugin 娉ㄥ唽銆丷ecipe Category 瀹炵幇銆丷ecipe Transfer銆丆atalyst 鍏宠仈銆丟host 鎷栨嫿銆佽繍琛屾椂浜や簰銆傚綋闇€瑕佹坊鍔?JEI 閰嶆柟鏌ョ湅銆佹満鍣ㄩ厤鏂瑰吋瀹规椂璋冪敤鏈?skill銆?
---

# JEI 闆嗘垚寮€鍙戞寚鍗?
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
                .addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 20)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(MyMachineRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        // 鑷畾涔夌粯鍒?    }
}
```

## Recipe Transfer

### 鍩虹 Transfer锛堥€傚悎鏍囧噯 slot 瀹瑰櫒锛?```java
@Override
public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(
            MyMachineContainer.class,
            ModMenuTypes.MY_MACHINE.get(),
            GoetyMechRecipeTypes.MY_MACHINE,
            0,     // recipeSlotStart
            9,     // recipeSlotCount
            9,     // inventorySlotStart
            36     // inventorySlotCount
    );
}
```

### 楂樼骇 Transfer锛堝畬鍏ㄦ帶鍒讹級
瀹炵幇 `IRecipeTransferInfo` 澶勭悊闈炶繛缁Ы浣嶏紝鎴栧疄鐜?`IRecipeTransferHandler` 瀹屽叏鑷畾涔夈€?
## Recipe Catalyst

灏嗘満鍣?鏂瑰潡鍏宠仈鍒?Recipe Type锛岀偣鍑绘柟鍧楀嵆鍙煡鐪嬬浉鍏抽厤鏂广€?
```java
@Override
public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.MY_MACHINE.get()),
            GoetyMechRecipeTypes.MY_MACHINE
    );
}
```

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

## 杩愯鏃朵氦浜?
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