# BreedingNestBlockEntity — goetypunk

## 身份

- 类型：block-entity
- Java 类：BreedingNestBlockEntity
- 关联方块：BreedingNestBlock (`docs/items/block/breeding-nest.md`)
- 首次创建：2026-06-29

## 设计意图

育巢的方块实体，处理能量累积、食物消耗、产物输出逻辑。
支持漏斗/Create 输入（WorldlyContainer + SidedInvWrapper）。

## Timeline

- **2026-06-29** — 创建，实现能量累积 + 自动产出 + NBT 持久化 + 网络同步 + 容器兼容
- **2026-06-29** — 增加输入冷却、转化延时 2s、outputStack 无限等待提取、红石粒子
