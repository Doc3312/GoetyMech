# Recipe Transfer + Catalyst

## Recipe Transfer

让 JEI 的 [+] 按钮将配方材料自动移到 Container 合成格。

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

```java
// 实现 IRecipeTransferInfo 处理非连续槽位
// 或实现 IRecipeTransferHandler 完全自定义
registration.addRecipeTransferHandler(
        new MyCustomTransferHandler(),
        GoetyPunkRecipeTypes.MY_MACHINE
);
```

### 通用 Transfer（AE2 类自动合成）

```java
registration.addUniversalRecipeTransferHandler(new MyUniversalTransferHandler());
```

---

## Recipe Catalyst

将机器/方块关联到 Recipe Type，点击方块即可查看相关配方。

```java
@Override
public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    // 单个关联
    registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.MY_MACHINE.get()),
            GoetyPunkRecipeTypes.MY_MACHINE
    );

    // 一个催化剂关联多个 RecipeType
    registration.addRecipeCatalyst(
            ModBlocks.ALLOY_FURNACE.get(),
            GoetyPunkRecipeTypes.ALLOY_SMELTING,
            GoetyPunkRecipeTypes.ALLOY_BLASTING
    );
}
```
