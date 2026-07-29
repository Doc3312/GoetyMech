---
id: "_craft.20260720.gateway-offhand-use"
title: "Gateway Focus — offhand.use() 劫持方案"
status: verified
date: "20260720"
tags: ["Gateway", "Focus", "StructureTag", "StructureEyeItem", "Java"]
related_items: []
related_commits: []
supersedes: []
superseded_by: []
---

## 症状

<!-- TODO: human review — 描述问题现象 -->

## 根因

<!-- TODO: human review — 描述根本原因 -->

## 修复

<!-- TODO: human review — 描述修复方案 -->

## 经验提炼

1. **ItemStack 恢复**：`offhand.use()` 后需要 `player.setItemInHand(OFF_HAND, backup)` 恢复，
   因为物品可能被消耗或损坏。

2. **`signalTo` 不会被所有眼调用**：只有产生实体的眼才会调 `signalTo`。某些物品（如地图）不走此路径。

3. **跨维搜索**：
   - `offhand.use()` 只能搜索当前维度（因为眼睛实体在当前维度生成）
   - 需要额外 Mixin 拦截 `ServerLevel.findNearestMapStructure(TagKey, ...)` 获取 tag →
     然后手动遍历 `server.getAllLevels()`
   - `TagKey` 重载 vs `HolderSet` 重载：某些模组可能直接调 `HolderSet` 重载，
     此时 TagKey 捕获不到，需回退到 signalTo pos

4. **Mixin 数量**：该方案需要 3 个 Mixin（vanilla EyeOfEnder / Goety EyeItemEntity / ServerLevel），
   比原方案多，但通用性强。

5. **搜索半径**：由眼睛自身代码决定（通常 100 chunk），非我们控制。
