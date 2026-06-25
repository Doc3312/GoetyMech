# Overlay buffer 排序：MultiBufferSource RenderType 渲染顺序不可控

status: verified

## 症状

ISTER 中用 `entityCutoutNoCull(overlayTexture)` 叠加 overlay quad → overlay 在某些物品上被胸甲覆盖，位置不稳定。

## 诊断过程

1. 怀疑 depth test → 尝试用各种 RenderType（`entityTranslucent`、`entityCutoutNoCullZOffset`、自定义 DepthTestStateShard）→ 无效
2. 怀疑 `RenderType.NO_DEPTH_TEST` 等字段 → 发现全是 `protected static final`，无法从外部访问
3. 怀疑 buffer flush 顺序 → 检查 Forge ItemRenderer 源码 → 发现 `renderGuiItem` 中 `render` → `fillStackedBar` → `renderDecorator`，且在 renderDecorator 前没有 flush
4. 手动 flush 胸甲 buffer 后再 `endBatch(chestplateRt)` → 失败，因为 endBatch 释放了 buffer
5. 读 Forge `IItemDecorator` 文档 → 发现它在 `GuiGraphics.drawManaged()` 内部被调用，drawManaged 包含 flush()

## 根因

**MultiBufferSource buffer ordering**：
- 每个 RenderType 对应独立 GPU BufferBuilder
- `endBatch()` 按 internal map 顺序遍历提交（HashMap → 非确定性迭代顺序）
- overlay buffer 可能在胸甲 buffer 之前提交 → GPU 先画 overlay，后画胸甲 → overlay 被覆盖
- 这不是 depth test 问题（即使 depth 相同，GPU 按提交顺序绘制）

## 解决方案：Forge IItemDecorator

- `IItemDecorator.render()` 在 `GuiGraphics.flush()` 之后调用
- 所有 item buffer（包括胸甲 quads 和 glint）都已经提交到屏幕
- overlay 通过 `GuiGraphics.blit()` 渲染 → `RenderType.guiTextured()` → NO_DEPTH_TEST + TRANSLUCENT_TRANSPARENCY
- z=400 translate 保证在耐久条/物品计数之上
- 100% 在最上层，不受 buffer 排序影响

## 教训

- ISTER 适合**替换**物品模型，不适合**叠加**渲染层
- 叠加层（overlay/装饰/标记）始终用 IItemDecorator
- `RenderType` 的 `protected` 字段（NO_DEPTH_TEST等）不可绕过，但 `RenderType.guiTextured()` 等 public factory methods 可实现类似效果
- `GuiGraphics.blit()` 是 overlay 渲染的正确选择（自动处理 depth/alpha/UV）
