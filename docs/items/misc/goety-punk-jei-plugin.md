# GoetyPunk JEI Plugin

`@JeiPlugin` 类，注册 JEI ingredient info 等信息。

## Timeline
- **2026-06-24** — 创建。`registerCategories()` + `registerRecipes()` 空实现，`addIngredientInfo()` 显示 prototype_mimic 合成说明。JEI 依赖改为 full forge jar（API jar 缺少 IModPlugin）。
- **2026-06-26** — `compileOnly` 保留完整 forge jar，加回 `runtimeOnly`。JEI 在 dev 环境可用，生产环境为可选依赖。
