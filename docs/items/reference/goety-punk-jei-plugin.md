# GoetyMech JEI Plugin

`@JeiPlugin` 类，注册 JEI ingredient info 等信息。

## Timeline
- **2026-06-24** — 创建。`registerCategories()` + `registerRecipes()` 空实现，`addIngredientInfo()` 显示 prototype_mimic 合成说明。JEI 依赖改为 full forge jar（API jar 缺少 IModPlugin）。
- **2026-06-26** — `compileOnly` 保留完整 forge jar，加回 `runtimeOnly`。JEI 在 dev 环境可用，生产环境为可选依赖。
- **2026-06-26** — 新增 `GoetyMechRecipeTypes` + `MimicCombinationCategory` + `MimicDeconstructionCategory`。Plugin 注册两个自定义 Recipe Category，`registerRecipes` 动态枚举所有胸甲物品作为组合配方输入候选。
- **2026-07-02** — 注册 6 条血肉晶匣循环显示配方（3 tier × 2 变换：B→C, C→A）。使用 `CursedInfuserRecipes` 构造带 NBT 的输入/输出 ItemStack，注册到 Goety 的 `JeiRecipeTypes.CURSED_INFUSER`。
