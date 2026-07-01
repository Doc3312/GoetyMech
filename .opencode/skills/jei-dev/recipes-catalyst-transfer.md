# Recipe Transfer + Catalyst

## Recipe Transfer

璁?JEI 鐨?[+] 鎸夐挳灏嗛厤鏂规潗鏂欒嚜鍔ㄧЩ鍒?Container 鍚堟垚鏍笺€?

### 鍩虹 Transfer锛堥€傚悎鏍囧噯 slot 瀹瑰櫒锛?

```java
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

```java
// 瀹炵幇 IRecipeTransferInfo 澶勭悊闈炶繛缁Ы浣?
// 鎴栧疄鐜?IRecipeTransferHandler 瀹屽叏鑷畾涔?
registration.addRecipeTransferHandler(
        new MyCustomTransferHandler(),
        GoetyMechRecipeTypes.MY_MACHINE
);
```

### 閫氱敤 Transfer锛圓E2 绫昏嚜鍔ㄥ悎鎴愶級

```java
registration.addUniversalRecipeTransferHandler(new MyUniversalTransferHandler());
```

---

## Recipe Catalyst

灏嗘満鍣?鏂瑰潡鍏宠仈鍒?Recipe Type锛岀偣鍑绘柟鍧楀嵆鍙煡鐪嬬浉鍏抽厤鏂广€?

```java
@Override
public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    // 鍗曚釜鍏宠仈
    registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.MY_MACHINE.get()),
            GoetyMechRecipeTypes.MY_MACHINE
    );

    // 涓€涓偓鍖栧墏鍏宠仈澶氫釜 RecipeType
    registration.addRecipeCatalyst(
            ModBlocks.ALLOY_FURNACE.get(),
            GoetyMechRecipeTypes.ALLOY_SMELTING,
            GoetyMechRecipeTypes.ALLOY_BLASTING
    );
}
```
