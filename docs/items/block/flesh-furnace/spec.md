# 血肉熔炉 — GoetyMech

## 身份

- 类型：block
- 注册类：ModBlocks.FLESH_FURNACE / ModItems.FLESH_FURNACE
- 方块类：FleshFurnaceBlock
- 方块实体：FleshFurnaceBlockEntity (IItemHandler)
- 首次创建：2026-06-14

## 设计意图

血肉主题的特殊熔炉，具有与原版熔炉不同的处理逻辑和视觉效果。
方块实体处理熔炉逻辑、Create 动力接口，支持链接数据、坐标哈希分散 tick、漏斗兼容。

## Timeline

- **2026-06-27** — 修复 C1: handlers 数组顺序与 `get3DDataValue()` 索引对齐，修复 EAST 面崩溃和方向映射错乱
- **2026-06-29** — 修复：setItem stack.split(1) 导致 Create 漏斗不消耗物品（改用 stack.copyWithCount(1)）
- **2026-06-29** — 修复：cachedLinkRange 未初始化导致连接距离为 0（添加 onConfigLoad handler）
