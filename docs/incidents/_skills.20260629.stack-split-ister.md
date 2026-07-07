# stack.split(n) in setItem 破坏原始引用

- status: verified
- 日期: 2026-06-29
- 涉及: FleshFurnaceBlockEntity + SidedInvWrapper + Create 漏斗

## 现象

Create 漏斗从传送带向血肉熔炉输入扭曲胚胎时，物品在传送带上不动，但熔炉持续输出成熟胚胎。

## 根因

`FleshFurnaceBlockEntity.setItem()` 中写的是 `cookingStack = stack.split(1)`。

`SidedInvWrapper.insertItem()` 在 B2 分支（槽位空、可全部接受）将原始 stack 引用直接传入 `setItem`（不做 `.copy()`）。`stack.split(1)` 内部调 `this.shrink(1)`，修改了原始 ItemStack 的 count，导致 `TransportedItemStack.stack`（Create 传送带数据结构）被置空。

漏斗检查返回值为 `ItemStack.EMPTY`，认为"插入成功"，但传送带侧的内部数据已损坏，视觉上物品不移动。

## 教训

任何实现 `setItem`/`setInventorySlotContents` 时，如果需要限制数量：
- ❌ `stack.split(n)` — 修改原始引用，副作用影响调用方
- ✅ `stack.copyWithCount(n)` — 创建独立副本，不修改原引用

此坑在所有实现 `WorldlyContainer` 且与 Create 或任何 IItemHandler 管道交互的模组中都存在。
