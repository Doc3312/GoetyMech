# Prototype Mimic

模仿盔甲外观 + 属性的 ElytraItem。

## Timeline
- **2026-06-24** — Recipe 联调: `createFrom()` 供 MimicCombinationRecipe/MimicDeconstructionRecipe 调用；JEI ingredient info 显示合成说明。Recipe 加载仍不成功。
- **2026-06-26** — 追加 `appendHoverText()`，tooltip 显示来源胸甲名 + 继承属性/附魔数。
- **2026-06-26** — `createFrom()` 新增 `TAG_SOURCE_REGISTRY` 存注册名；新建 `getSourceStack()`。IST ER (`MimicItemRenderer`) 实现动态渲染源胸甲图标 + 叠加覆盖层。删除 `MIMIC_ENABLED` 配置项。
- **2026-06-26** — Overlay 从 ISTER 迁移至 Forge `IItemDecorator`（`MimicItemDecorator`）。根因：MultiBufferSource 不同 RenderType buffer 提交顺序不可控，overlay buffer 可能在胸甲 buffer 之前 flush 导致被覆盖。`IItemDecorator` 在 `GuiGraphics.flush()` 之后渲染，保证在最上层。`MimicItemRenderer` 中 overlay 相关代码（`OVERLAY_TEXTURE`、`OVERLAY_RENDER_TYPE`、flush 逻辑）全部移除。
- **2026-06-26** — `HumanoidArmorLayerMixin`：@Inject HEAD of `renderArmorPiece`，绕过 `instanceof ArmorItem` 检查，渲染源胸甲 3D armor model。`ElytraLayerMixin`：融合时取消鞘翅翅膀。
- **2026-06-29** — 删 `ElytraLayerMixin`（冗余）：`PrototypeRenderEvents.onRenderPlayer()` 的 flat 纹理渲染在融合态也会显示，和 armor model 叠加。改为在 `onRenderPlayer()` 检测 `PrototypeMimicItem && source 非空` 时 `return` 跳过 flat 纹理，融合态仅展示 armor model，非融合态展示默认 flat 纹理。
