# 成熟胚胎 — GoetyMech

## 身份

- 类型：item
- 注册类：ModItems.MATURE_EMBRYO
- Java 类：MatureEmbryoItem (extends Item)
- 首次创建：2026-06-14

## 设计意图

进阶召唤物品：手持右键在瞄准位置生成更高级的 Goety 仆从。同时作为点将台系统的消耗品。

## 演化记录

### 2026-06-15 — 引入作为点将台消耗品

- 改动：MatureEmbryo 被 SquadBarracks 系统引用，作为编队召唤的消耗材料
- 原因：需要一种可堆叠、可配置的召唤消耗品来驱动点将台
- 关联：openspec squad-summon-system

### 2026-06-16 — 提取重复射线追踪逻辑

- 改动：从 MatureEmbryoItem.use() 中提取射线追踪定位逻辑到 MinionSpawnHelper.findSpawnPosition()
- 原因：与 TwistedEmbryoItem 存在完全重复的射线追踪代码
- 关联：openspec codebase-remediation
