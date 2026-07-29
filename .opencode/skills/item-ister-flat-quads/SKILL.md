---
name: item-ister-flat-quads
description: Flat item quad rendering (item/generated) via ISTER/BEWLR — overlay / texture / quad / UV / PNG / 纹理 / 叠加层 / 四边面渲染
---

# ★★★ 最高优先级：Flat Item Quad 渲染（ISTER / BEWLR）
**只要涉及任何纹理/PNG/叠加层/overlay/quad/图标渲染，必须优先加载本 skill。**

触发关键词（中英双语，任意匹配即触发）：
- 纹理 / texture / PNG / png / 叠图 / overlay
- ISTER / BEWLR / IClientItemExtensions
- `item/generated` / `item/handheld`
- flat item / 平面物品 / 物品渲染 / 图标
- quad / 四边面 / 顶点渲染 / vertex / UV mapping
- scale(1,-1,-1) / scale(-1,-1,-1) / Y flip / Y 翻转
- `renderModelLists` / `renderByItem` / BlockEntityWithoutLevelRenderer

---

## ★ 核心教训：永远不要对 flat item quads 使用 scale(1,-1,-1)

**`scale(1, -1, -1)` 是 Goety 的 EntityModel 3D 渲染模式，用于 EntityModel 或 HierarchicalModel（如法杖、仆从实体模型），不适用于 `item/generated` 或 `item/handheld` 的平面物品 quads。**

### 为什么错

| 手法 | 适用场景 | 后果 |
|------|---------|------|
| `translate(0.5) + scale(1,-1,-1)` → `renderModelLists` | Goety 的 **EntityModel**（实体 3D 模型） | 若用在 flat items → 胸甲旋转 180° + 偏移到 slot 角落 |
| `scale(1,-1,-1)` | `HierarchicalModel.renderToBuffer()` — 3D cube-based 模型 | 若用在 flat quads → Y 轴翻转导致位置错乱 |
| 什么都不做，直接 `renderModelLists` | **`item/generated` flat quads** | ✅ 正确 — 外层 `translate(-0.5)` 已自动居中 |

### 正确模式

ItemRenderer 对 ISTER 的调用路径：

```
render() → handleCameraTransforms(modelTransforms) → translate(-0.5) → renderByItem()
```

`translate(-0.5)` 已将坐标原点对准 slot 中心。`item/generated` 的 quads 定义在 0..16 空间，在此空间下自动居中。
```java
@Override
public void renderByItem(ItemStack stack, ItemDisplayContext transformType,
                         PoseStack poseStack, MultiBufferSource buffer,
                         int combinedLight, int combinedOverlay) {
    // ✅ 正确：无 translate(0.5)，无 scale(1,-1,-1)
    // 可选：对 source model 应用其自身的 display transforms
    sourceModel.getTransforms().getTransform(transformType).apply(false, poseStack);

    // 渲染源物品 quads：0..16 空间，已被外层 -0.5 居中
    // ⚠️ 不要在 ISTER 内渲染 overlay quad → buffer 排序不可控
    //    改用 Forge IItemDecorator（见下方「第二核心教训」章节）
    for (BakedModel pass : sourceModel.getRenderPasses(sourceStack, true)) {
        for (RenderType rt : pass.getRenderTypes(sourceStack, true)) {
            VertexConsumer vc = ItemRenderer.getFoilBuffer(buffer, rt, true, sourceStack.hasFoil());
            itemRenderer.renderModelLists(pass, sourceStack, combinedLight, combinedOverlay, poseStack, vc);
        }
    }
}
```

### 坐标速查表
| 坐标系 | 范围 | 1 单位 = | 使用场景 |
|--------|------|---------|---------|
| 模型 UV | 0..1 | 16 像素 | 纹理映射 |
| item/generated quad | 0..16 | 1 像素 | 物品模型顶点 |
| ISTER 居中空间 | 0..1 | 16 像素 | 叠加层 quad（在外层 translate(-0.5) 后的空间里，0..1 → -0.5..0.5 居中） |
| OpenGL 窗口 | 视口尺寸 | 取决于投影 | 最终屏幕像素 |

---

## Texture / RenderType 最佳实践
| 纹理类型 | RenderType | 说明 |
|---------|-----------|------|
| 不透明纹理 | `entityCutoutNoCull(TEXTURE)` | 无透明背景 |
| 半透明叠加（含 alpha） | `entityTranslucent(TEXTURE)` | 保留 PNG 透明通道 |
| 实体纹理路径 | `textures/entity/<name>.png` | 与 block atlas 分离 |
| 物品纹理路径 | `textures/item/<name>.png` | 不会自动 stitching，适合 ISTER |
| 引用方式 | `new ResourceLocation("modid", "textures/item/name.png")` | 路径含 `textures/`，不含 `.png` 扩展名 |

### UV 坐标系规则
OpenGL 纹理 Y 轴向下（UV 坐标），模型坐标 Y 轴向上：

```
UV(0,0) = 纹理左上角    →→     模型顶点 (x, 高y)
UV(0,1) = 纹理左下角    →→     模型顶点 (x, 低y)
```

因此 quad 顶点顺序为：**左下(0,1) → 左上(0,0) → 右上(1,0) → 右下(1,1)**

---

## ★ 第二核心教训：Overlay 渲染 → 用 Forge `IItemDecorator`，不要用 ISTER

**如果要在物品图标上叠加一个半透明 overlay（如病毒/附魔光效/标记等），使用 Forge 的 `IItemDecorator`，不要用 MultiBufferSource 在 ISTER 中追加 quad。**

### 根因：MultiBufferSource 的 RenderType 缓冲排序不可控
1. **每个 RenderType 对应一个独立的 GPU 缓冲池（BufferBuilder）**
2. **MultiBufferSource.endBatch()** 以 HashMap 顺序遍历所有 RenderType → 提交到 GPU
3. **HashMap 迭代顺序非确定性** → 不同 RenderType 的 buffer 提交到 GPU 的顺序不可预知
4. **如果 overlay 的 RenderType buffer 在 胸甲 RenderType buffer 之前 flush** → GPU 先画 overlay，再画胸甲 → overlay 被覆盖

### 尝试过的修复（全部失败）

| 方案 | 结果 |
|------|------|
| `flush()` 胸甲 buffer 后再渲染 overlay | ⚠️ `flush()` 后 buffer 被释放，需重建 |
| `endBatch()` / `end()` | ⚠️ 同样释放 buffer，后续渲染无效 |
| 尝试访问 `RenderType.NO_DEPTH_TEST` | ⚠️ `protected static final`，无法从外部访问 |
| 用 `RenderType.entityCutoutNoCull(overlayTexture)` 替代 | ⚠️ 仍然是不同 RenderType → 不同 buffer → 排序不可控 |

### ✅ 正确方案：Forge `IItemDecorator`

```
render() → item + glint (含 flush) → IItemDecorator.render() → flush()
```

**Forge 1.20.1 的 `IItemDecorator` 在 `GuiGraphics.flush()` 之后渲染**，此时所有 RenderType buffer（胸甲、glint、耐久条、物品计数）都已被 flush 到屏幕。Overlay 再通过 `GuiGraphics.blit()` 叠加 → 使用 `RenderType.guiTextured()` → NO_DEPTH_TEST + alpha blend → **保证在最上层**。

#### 实现要点

```java
public class MimicItemDecorator implements IItemDecorator {
    private static final ResourceLocation OVERLAY = 
        new ResourceLocation("GoetyMech", "textures/item/prototype_mimic_overlay.png");

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack,
                          int xOffset, int yOffset) {
        // xOffset, yOffset = slot 左上角位置（GUI 屏幕坐标）
        guiGraphics.pose().pushPose();
        // translate z=400: 确保在耐久条/物品计数/glint 上方
        guiGraphics.pose().translate(0, 0, 400);
        // blit: 16×16 覆盖整个 slot
        guiGraphics.blit(OVERLAY, xOffset, yOffset, 0, 0, 16, 16, 16, 16);
        guiGraphics.pose().popPose();
        return true; // 返回 true 表示应用了装饰
    }
}
```

#### 注册

```java
@SubscribeEvent
public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
    event.register(ModItems.PROTOTYPE_MIMIC.get(), new MimicItemDecorator());
}
```

### 何时用哪个
| 场景 | 方案 | 原因 |
|------|------|------|
| 替换物品模型（如胸甲渲染） | ISTER (BEWLR) | 需要完全控制模型和 quads |
| 在物品上叠加纹理/光效 | **IItemDecorator** | 不受 buffer 排序影响，保证在最上层 |
| 混合：3D 模型 + overlay | ISTER 做模型 + IItemDecorator 做 overlay | 分层渲染，各司其职 |

---

## 调试检查清单
当 overlay/texture/quad 渲染异常时：

- [ ] 是否错误使用了 `scale(1,-1,-1)`？→ 删除，flat quads 不需要
- [ ] 是否错误使用了 `translate(0.5)`？→ 删除，外层 -0.5 已居中
- [ ] overlay quad 坐标范围是 0..1 还是 0..16？→ 0..1（在外层 T(-0.5) 下映射到 -0.5..0.5 居中）
- [ ] RenderType 是否匹配纹理 alpha？→ 透明用 `entityTranslucent`，不透明用 `entityCutoutNoCull`
- [ ] UV 坐标是否 V 翻转？→ 底部顶点用 `uv(0,1)`，顶部用 `uv(0,0)`
- [ ] 是否有非 GUI display transform 干扰？→ 检查 `sourceModel.getTransforms().getTransform(type)`
- [ ] z 值是否太小导致 Z-fighting？→ overlay 用 `z=0.01`，胸甲 quads 在 `z=0`

## 相关技能

| 技能 | 典型切换场景 |
|------|-------------|
| `entity-render-dev` | 需要渲染实体模型/碰撞箱而非物品图标时 |
