# 育巢 — GoetyMech

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

### 2026-06-29 — 修复：输入冷却 + 满仓不弹物品
- 输入冷却 10 tick，防止漏斗灌入过快
- 移除输出 fallback 掉落，outputStack 无限等待侧面漏斗提取
- 能量满后 2s 转化延时 + 红石粒子
- burp 音效 1.5s 冷却
