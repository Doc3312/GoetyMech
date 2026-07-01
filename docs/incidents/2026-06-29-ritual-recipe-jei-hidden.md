# Goety 仪式配方 JEI 不显示 — 调试记录

status: verified

## 现象

所有 3 个 cycle 配方（诅咒注入器）和 Tier I 仪式配方在 JEI 显示正常，但 Tier II/III 仪式配方不出现。游戏日志无报错（无 ParseException）。

## 根因

两个独立问题：**配方 item ID 错误** + **物品缺纹理**。

### 问题 1：6 个 Goety 物品 ID 与源码不匹配

3 个 upgrade 配方 JSON 中以下 ID 写错：

| 错误 ID | 正确 ID | 来源（ModItems） |
|---------|---------|-----------------|
| `pale_metal_ingot` | `pale_steel_ingot` | line 64 |
| `dark_metal_ingot` | `dark_ingot` | line 65 |
| `night_core` | `heart_of_the_night` | line 94 |
| `unholy_cloth` | `unholy_fabric` | line 74 |
| `large_focus_bag` | `focus_pack` | line 202 |
| `pure_blood` | `unholy_blood` | line 124 |

这些错误 ID 对应的物品不存在（Goety 从未用这些名称注册过）。Goety 的配方解析器不报错，但跳过这些配方 → JEI 不枚举。

**教训**：Goety 的 RecipeType 没有 `ingredients` 严格校验，无效 ingredient item ID 静默失败。必须对照 Goety `ModItems.java` 源码逐项确认。

### 问题 2：物品缺纹理

Tier I 使用 `goety:craft` 仪式类型始终可用，但 `flesh_casket_1/2/3` 没有对应 `textures/item/` 下的 PNG，导致物品栏显示紫黑块。这对于 JEI 显示虽然有影响，但不是根本原因（Tier I 在缺纹理时仍显示）。

**教训**：注册新物品后立即创建占位纹理，否则视觉异常会干扰问题定位。

## 修复

1. 修正 3 个 JSON 中的 6 个 item ID
2. 创建 `assets/GoetyMech/textures/item/flesh_casket_{1,2,3}.png` 占位纹理（3×3px 色块，复制自 prototype.png）
3. 重新打包启动后 3 个升级配方全部显示

## 验证

- `runClient` 启动到世界
- JEI 查看所有 6 个配方（3 cycle + 3 ritual）均可见
- 日志中无 `flesh_casket` 相关异常

## 相关文件与路径

- `src/main/resources/data/GoetyMech/recipes/flesh_casket_1_to_2.json`
- `src/main/resources/data/GoetyMech/recipes/flesh_casket_2_to_3.json`
- `src/main/resources/assets/GoetyMech/textures/item/flesh_casket_{1,2,3}.png`
- `D:\Libcode\goety\src\main\java\com\Polarice3\Goety\init\ModItems.java`（验证源）
