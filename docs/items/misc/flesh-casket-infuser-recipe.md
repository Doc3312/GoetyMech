# Flesh Casket Infuser Recipe — goetypunk

## 身份

- 类型：CursedInfuserRecipes（诅咒注入器配方）
- 注册：ModRecipeSerializers.FLESH_CASKET_INFUSER
- Java 类：FleshCasketInfuserRecipe
- 首次创建：2026-06-29

## 设计意图

实现晶匣触发类型的循环切换（A→B→C→A），放入诅咒注入器后自动切换触发模式。

## 演化记录

### 2026-06-29 — 初始创建

- 改动：FleshCasketInfuserRecipe + FleshCasketInfuserRecipeSerializer。`assemble()` 复制输入 NBT 后执行 `cycleTriggerType()`。配方 JSON 3 个（flesh_casket_cycle_1/2/3）。
- 原因：替代 9 变体注册方式，通过 NBT 切换触发类型
- 关联：commit b10c2c9

### 2026-07-02 — 修复升级清空聚晶 + JEI 图标修正

- 改动：`assemble()` 新增 ITEM_HANDLER capability 逐槽复制聚晶栈；覆盖 `getResultItem()` 返回已循环类型的 output。
- 原因：`this.result.copy()` 创建空 handler，setTag 不恢复内部物品；JEI 显示默认 A 类型图标。
- 关联：commit 86a72e3
