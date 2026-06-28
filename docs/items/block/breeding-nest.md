# 育巢 — goetypunk

## 身份

- 类型：block
- 注册类：ModBlocks.BREEDING_NEST / ModItems.BREEDING_NEST
- Java 类：BreedingNestBlock
- 方块实体：BreedingNestBlockEntity
- 首次创建：2026-06-29

## 设计意图

将食物/腐肉等可恢复饱食度的物品转化为扭曲胚胎，补全食物→胚胎→成熟兵种转化链。

## 演化记录

### 2026-06-29 — 创建
- 方块、方块实体、HUD、注册、配置、资源文件全部实现
- 能量基于食物 hunger 值累积，满 25 hunger 产出 1 个扭曲胚胎
- 无 GUI，右键放食物，破坏无掉落
- 支持漏斗/Create 输入
- HUD 显示充能进度
