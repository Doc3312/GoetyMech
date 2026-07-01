# FleshFurnaceBlockEntity — GoetyMech

## 身份

- 类型：block-entity
- Java 类：FleshFurnaceBlockEntity
- 关联方块：FleshFurnaceBlock (`docs/items/block/flesh-furnace.md`)
- 首次创建：2026-06-14

## 设计意图

血肉熔炉的方块实体，处理熔炉逻辑、Create 动力接口（IItemHandler）。
支持链接数据、坐标哈希分散 tick、漏斗兼容。

## Timeline

- **2026-06-27** — 修复 C1: handlers 数组顺序与 `get3DDataValue()` 索引对齐，修复 EAST 面崩溃和方向映射错乱。
