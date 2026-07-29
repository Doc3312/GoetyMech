# 育巢 — GoetyMech

## 身份

- 类型：block
- 注册类：ModBlocks.BREEDING_NEST / ModItems.BREEDING_NEST
- 方块类：BreedingNestBlock
- 方块实体：BreedingNestBlockEntity (WorldlyContainer + SidedInvWrapper)
- 首次创建：2026-06-29

## 设计意图

将食物/腐肉等可恢复饱食度的物品转化为扭曲胚胎，补全食物→胚胎→成熟兵种转化链。
方块实体处理能量累积、食物消耗、产物输出逻辑。

## Timeline

- **2026-06-29** — 创建，实现能量累积 + 自动产出 + NBT 持久化 + 网络同步 + 容器兼容
- **2026-06-29** — 增加输入冷却 10 tick、转化延时 2s、outputStack 无限等待提取、红石粒子
