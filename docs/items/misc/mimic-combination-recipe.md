# Mimic Combination Recipe

prototype + chestplate → prototype_mimic。继承盔甲属性和 NBT。

## Timeline
- **2026-06-24** — 初始实现。`fromJson()` 解析 `result` 字段（Goety 模式），`assemble()` 调 `PrototypeMimicItem.createFrom()`。注册为 `mimic_combination` serializer。
