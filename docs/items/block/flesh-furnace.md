# 血肉熔炉 — GoetyMech

## 身份

- 类型：block
- 注册类：ModBlocks.FLESH_FURNACE / ModItems.FLESH_FURNACE
- Java 类：FleshFurnaceBlock
- 方块实体：FleshFurnaceBlockEntity
- 首次创建：2026-06-14

## 设计意图

血肉主题的特殊熔炉，具有与原版熔炉不同的处理逻辑和视觉效果。

## 演化记录

### 2026-06-29 — 修复：setItem stack.split(1) 导致 Create 漏斗不消耗物品
- **原因**：`setItem()` 中 `stack.split(1)` 修改了 `SidedInvWrapper` 传入的原始 ItemStack 引用，导致 Create 传送带侧的 `TransportedItemStack.stack` 被设为空栈，漏斗认为未成功传输
- **修复**：改为 `stack.copyWithCount(1)`，创建独立副本，不修改原引用
- **关联**：`FleshFurnaceBlockEntity.java:232` + `:559`

### 2026-06-29 — 修复：cachedLinkRange 未初始化导致连接距离为 0
- **原因**：`refreshCache()` 只在 `ModConfigEvent.Reloading` 时调用，缺少 `ModConfigEvent.Loading`，`cachedLinkRange` 保持 `int` 默认值 `0`
- **修复**：添加 `onConfigLoad(ModConfigEvent.Loading)` handler
- **关联**：`ModConfig.java:135-138`
