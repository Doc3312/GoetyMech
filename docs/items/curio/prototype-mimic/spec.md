# Prototype Mimic — 原型拟态

## 身份

- 类型：curio / 胸甲
- 注册名：`goetymech:prototype_mimic`
- Java 类：`com.goetymech.addon.common.item.PrototypeMimicItem`
- 渲染器：`com.goetymech.addon.client.MimicItemRenderer`（BEWLR/ISTER）

## 设计意图

`Prototype Mimic` 是 `Prototype` 胸甲的拟态版本。它继承原胸甲的飞行能力，同时把外观伪装成另一件胸甲（“源物品”）。源物品的模型、附魔、属性修饰符都会被记录并模拟显示。

## 关键机制

- **源物品记录**：合成时把源胸甲的 registry id、名称、属性修饰符、附魔写入 NBT。
- **外观伪装**：通过 `MimicItemRenderer` 在 BEWLR 中读取源物品模型并渲染。
- **Overlay 标记**：`MimicItemDecorator` 在物品栏中绘制黑光病毒 overlay，提示这是一件拟态物品。
- **属性继承**：穿戴时从 NBT 恢复源物品的属性修饰符。

## 渲染约束

- 模型 `prototype_mimic.json` 必须使用 `builtin/entity` 作为 parent，否则 BEWLR 不会被触发。
- 为了同时满足“手持姿势正确”和“GUI 平铺显示”，JSON 中按 `ItemDisplayContext` 分别定义 transform：
  - 手持/地面/展示框/头戴：使用 `item/generated` 标准 transform。
  - GUI：不定义 transform（identity），让源模型平铺显示在槽位中。

## 关联文件

- `src/main/java/com/goetymech/addon/common/item/PrototypeMimicItem.java`
- `src/main/java/com/goetymech/addon/client/MimicItemRenderer.java`
- `src/main/java/com/goetymech/addon/client/MimicItemDecorator.java`
- `src/main/resources/assets/goetymech/models/item/prototype_mimic.json`
