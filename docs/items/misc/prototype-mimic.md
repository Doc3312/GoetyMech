# Prototype Mimic

模仿盔甲外观 + 属性的 ElytraItem。

## Timeline
- **2026-06-24** — Recipe 联调: `createFrom()` 供 MimicCombinationRecipe/MimicDeconstructionRecipe 调用；JEI ingredient info 显示合成说明。Recipe 加载仍不成功。
- **2026-06-26** — 追加 `appendHoverText()`，tooltip 显示来源胸甲名 + 继承属性/附魔数。
- **2026-06-26** — `createFrom()` 新增 `TAG_SOURCE_REGISTRY` 存注册名；新建 `getSourceStack()`。IST ER (`MimicItemRenderer`) 实现动态渲染源胸甲图标 + 叠加覆盖层。删除 `MIMIC_ENABLED` 配置项。
