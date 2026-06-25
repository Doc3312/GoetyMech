# MimicItemRenderer — goetypunk

## 身份

- 类型：misc
- Java 类：MimicItemRenderer
- 首次创建：2026-06-25

## 设计意图

Mimic 物品的 BER（BlockEntityWithoutLevelRenderer），负责渲染伪装状态下的物品外观。

## 演化记录

### 2026-06-25 — 创建

- 改动：创建 MimicItemRenderer，处理 Mimic 物品的伪装渲染
- 原因：Mimic 需要根据伪装状态动态切换渲染
- 关联：openspec mimic-system
