---
name: jei-dev
description: JEI / recipe display / ghost drag
---

# JEI 开发指南 (Forge 1.20.1 / JEI 15.x)

> 快速排除：当前任务如果不涉及 JEI 配方展示、Recipe Category、Recipe Transfer 或 Ghost 拖拽，请跳过本 skill。

## 适用场景

- 为自定义机器/配方添加 JEI 配方展示
- 实现 Recipe Transfer（[+] 按钮自动填充合成格）
- 注册 Recipe Catalyst（方块 ↔ 配方关联）
- 集成自定义 GUI 到 JEI（Ghost 拖拽、点击区域）
- 运行时动态控制 JEI（隐藏/显示配方）

## 文档索引

| 文件 | 内容 |
|------|------|
| [plugin-category.md](plugin-category.md) | Plugin 类 + RecipeType + Recipe Category 实现 + 注册 Recipes |
| [recipes-catalyst-transfer.md](recipes-catalyst-transfer.md) | Recipe Transfer + Recipe Catalyst |
| [gui-runtime.md](gui-runtime.md) | GUI 集成 + 运行时交互 + IJeiHelpers + 完整骨架 |

## 注册阶段（IModPlugin 方法调用顺序）

| 方法 | 用途 |
|---|---|
| `registerItemSubtypes` | 物品 NBT 子类型 |
| `registerIngredients` | 自定义 Ingredient |
| `registerCategories` | 注册 Recipe Category |
| `registerVanillaCategoryExtensions` | 扩展原版 Category |
| `registerGuiHandlers` | GUI 集成 |
| `registerRecipeTransferHandlers` | 配方转移 |
| `registerRecipeCatalysts` | 关联机器与 Recipe Type |
| `registerRecipes` | 注册 Recipes + Info 页 |
| `registerRuntime` | 运行时注册 |
| `onRuntimeAvailable` | 运行时交互 |

所有方法有默认空实现，只需覆盖需要的。

## 依赖源代码路径

所有前置模组的完整源代码在 `D:\Libcode\` 目录下，可直接查阅而非反编译：

| 源码目录 | 内容 |
|----------|------|
| `D:\Libcode\Create-mc1.20.1-dev\Create-mc1.20.1-dev\` | Create 模組原始码 |
| `D:\Libcode\Goety-2-1.20\Goety-2-1.20\` | Goety 2.x 原始码 |

---

## JEI 15.x API 坐标

| 包 | 说明 |
|---|---|
| `mezz.jei.api` | 核心接口 |
| `mezz.jei.api.constants` | 常量（VanillaTypes, VanillaRecipeTypes） |
| `mezz.jei.api.gui.builder` | 布局构建器 |
| `mezz.jei.api.gui.drawable` | 可绘制元素 |
| `mezz.jei.api.gui.ingredient` | 槽位交互 |
| `mezz.jei.api.helpers` | 工具类 |
| `mezz.jei.api.recipe` | 配方核心 |
| `mezz.jei.api.recipe.category` | Category 接口 |
| `mezz.jei.api.recipe.transfer` | 配方转移 |
| `mezz.jei.api.registration` | 注册接口 |
| `mezz.jei.api.runtime` | 运行时 |

## 相关技能

| 技能 | 典型切换场景 |
|------|-------------|
| `create-dev` | 添加 Create 机器的 JEI 配方展示/加工动画时 |
| `goety-addon-dev` | 添加聚晶/法术的 JEI 配方展示或幽灵拖拽时 |
