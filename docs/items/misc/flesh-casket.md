# 血肉魔匣 — goetypunk

## 身份

- 类型：curio
- 注册类：ModItems.FLESH_CASKET_A / B / C
- Java 类：FleshCasketItem (extends Item, implements ICurioItem)
- 首次创建：2026-06-23

## 设计意图

Curios 饰品槽位的自动施法容器。内置 1/3/5 格（按阶位）存储 Focus，周期自动施法。三阶循环升级：A→B→C→A，配方在 `data/goety/recipes/` 下。

## 演化记录

### 2026-06-23 — 初始设计与注册

- 改动：创建 FleshCasketItem + FleshCasketItemCapability + FleshCasketItemHandler + FleshCasketContainer + FleshCasketEvents + AutoCastDummyWand + TargetingRegistry + TriggerType + ModItems 注册 + 三阶模型 + 三阶循环升级配方 + ModConfig
- 原因：需要一个可以自动施法的 Curios 容器饰品，解放手持槽位
- 关联：14 new files, 4 modified (commit 0ec1b5d)

### 2026-06-23 — 修复 TargetingRegistry 无效字段引用

- 改动：修复 3 个无效的字段引用
- 原因：初始代码中引用还未注册的字段
- 关联：commit 6824236
