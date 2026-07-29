---
id: "remote_storage_focus.20260730.deposit-crash-persistence"
title: "DepositProvider Crash + Persistence Bug"
status: verified
date: "20260730"
tags: ["DepositProvider", "Crash", "Persistence", "Bug", "StackOverflow"]
related_items: []
related_commits: []
supersedes: []
superseded_by: []
---

## 症状

<!-- TODO: human review — 描述问题现象 -->

## 根因

### Bug 1: StackOverflow — 仓满崩服

`DepositProvider` 的 `removed()` 内调用了 `NetworkHooks.openScreen()` 重新打开 GUI，但 `openScreen()` 会先关闭当前容器 → 递归触发 `removed()` → 栈溢出。

### Bug 2: 区块卸载/断线丢物品

`LogisticallyLinkedBehaviour.getAllPresent()` 只遍历已加载区块。仓库区块卸载时返回空集合，`depositItems()` 直接 `return`，`SimpleContainer` 中的 `ItemStack` 随 GC 消失。断线同理。

## 修复

- 新建 `DepositSavedData extends SavedData`，存于主世界 `DimensionDataStorage`
- `depositItems()` 有剩余时写入 SavedData + 玩家消息提示，不再 `openScreen()`
- 再次打开 GUI 时从 SavedData 加载恢复

## 经验提炼

1. **永远不要在 `Container.removed()` 中调用 `NetworkHooks.openScreen()`**。这是已知的反模式，Forge 的 `openScreen` 会关闭当前菜单再打开新菜单，导致递归。
2. **`SimpleContainer` 是纯内存对象**，不持久化到任何地方。崩溃/断线/区块卸载时必丢物品。替代方案：
   - `WorldSavedData` (最可靠，`setDirty()` 后主动落盘)
   - Player Capability (自动随玩家存档序列化)
   - `Player.getPersistentData()` (简单 NBT)
3. **`LogisticallyLinkedBehaviour.getAllPresent()` 静默忽略未加载区块**。调用方必须自行处理空结果的情况，而不是当作"无链接"处理。
