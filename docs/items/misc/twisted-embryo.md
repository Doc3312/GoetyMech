# 扭曲胚胎 — goetypunk

## 身份

- 类型：item
- 注册类：ModItems.TWISTED_EMBRYO
- Java 类：TwistedEmbryoItem (extends Item)
- 首次创建：2026-06-14

## 设计意图

基础召唤物品：手持右键在瞄准位置生成对应的 Goety 仆从。与成熟胚胎形成"基础/进阶"配对。

## 演化记录

### 2026-06-16 — 提取重复射线追踪逻辑

- 改动：从 TwistedEmbryoItem.use() 中提取射线追踪定位逻辑到 MinionSpawnHelper.findSpawnPosition()
- 原因：与 MatureEmbryoItem 存在完全重复的射线追踪代码
- 关联：openspec codebase-remediation
