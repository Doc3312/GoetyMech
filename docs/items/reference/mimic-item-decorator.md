# MimicItemDecorator — GoetyMech

## 身份

- 类型：misc
- Java 类：MimicItemDecorator
- 首次创建：2026-06-25

## 设计意图

Mimic 物品的装饰器，负责在物品栏/背包中显示伪装的物品外观。

## 演化记录

### 2026-06-25 — 创建

- 改动：创建 MimicItemDecorator
- 原因：Mimic 需要欺骗外观显示为被伪装的物品
- 关联：openspec mimic-system

### 2026-07-01 — 修复 overlay 图层过高问题

- 改动：`translate(0, 0, 400)` → `translate(0, 0, 0)`
- 原因：z=400 导致 overlay 遮蔽手持物品预览和鼠标悬停 tooltip。IItemDecorator 已在 `GuiGraphics.flush()` 后执行，draw order 保证在最上层，无需高 z 值
- 关联：mimic-overlay-z-fix
