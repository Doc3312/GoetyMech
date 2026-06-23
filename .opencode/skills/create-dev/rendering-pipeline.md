# BESR 渲染管线与 Flywheel

## 目录

- [SafeBlockEntityRenderer](#safeblockentityrenderer)
- [KineticBlockEntityRenderer](#kineticblockentityrenderer)
- [CachedBuffers API](#cachedbuffers-api)
- [PartialModel 系统](#partialmodel-系统)
- [标准渲染模式对照](#标准渲染模式对照)
- [Flywheel Visual 系统](#flywheel-visual-系统)
- [Block Model 与纹理 UV 惯例](#block-model-与纹理-uv-惯例)

---

## SafeBlockEntityRenderer

```
内容/foundation/blockEntity/renderer/SafeBlockEntityRenderer.java
```

### render() 是 final

```java
public final void render(T be, float partialTicks, PoseStack ms,
    MultiBufferSource bufferSource, int light, int overlay)
```

- **`render()` 是 `final`**——禁止子类 override！
- 改为实现 `renderSafe()` 抽象方法
- `render()` 先检查 `isInvalid(be)`，无效直接 return

### isInvalid() 检查

```
内容/foundation/blockEntity/renderer/SafeBlockEntityRenderer.java:30
```

```java
return !be.hasLevel() || be.getBlockState().getBlock() == Blocks.AIR;
```

- 检查 BE 是否有 level 以及 block 是否已被移除（设为空气）
- 两个条件任一满足则跳过渲染

---

## KineticBlockEntityRenderer

```
内容/kinetics/base/KineticBlockEntityRenderer.java
```

### renderSafe() 核心逻辑

```java
// 第 48-55 行
protected void renderSafe(T be, float partialTicks, PoseStack ms,
    MultiBufferSource buffer, int light, int overlay) {
    if (VisualizationManager.supportsVisualization(be.getLevel())) return;
    BlockState state = getRenderedBlockState(be);
    RenderType type = getRenderType(be, state);
    renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);
}
```

**Flywheel 检查位置**: 第一行——如果 Flywheel visual 可用，直接 return，BESR 不渲染。渲染由 Flywheel 的 visual 类接管。

**Flywheel active**: `VisualizationManager.supportsVisualization(level)` 返回 true → BESR 跳过
**Flywheel inactive**: BESR 正常渲染旋转模型

### getRotatedModel() 默认实现

```java
// 第 72-74 行
protected SuperByteBuffer getRotatedModel(T be, BlockState state) {
    return CachedBuffers.block(KINETIC_BLOCK, state);
}
```

返回 `CachedBuffers.block(KINETIC_BLOCK, state)`——完整的 block 模型（带 blockstate 旋转）

### 旋转角度计算

```java
// 第 87-92 行
public static float getAngleForBe(KineticBlockEntity be, BlockPos pos, Axis axis) {
    float time = AnimationTickHolder.getRenderTime(be.getLevel());
    float offset = getRotationOffsetForPosition(be, pos, axis);
    return ((time * be.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
}
```

- `time * speed * 3/10`: 转换为渲染角度
- `% 360 / 180 * PI`: 归一化到弧度
- `getRotationOffsetForPosition()`: 加上 `rotationOffset`（位置奇偶偏移）和 `be.getRotationAngleOffset()`（自定义偏移）

### standardKineticRotationTransform()

```java
// 第 94-100 行
public static SuperByteBuffer standardKineticRotationTransform(
    SuperByteBuffer buffer, KineticBlockEntity be, int light) {
    Axis axis = ((IRotate) be.getBlockState().getBlock()).getRotationAxis(be.getBlockState());
    return kineticRotationTransform(buffer, be, axis, getAngleForBe(be, pos, axis), light);
}
```

### kineticRotationTransform()

```java
// 第 102-124 行
public static SuperByteBuffer kineticRotationTransform(
    SuperByteBuffer buffer, KineticBlockEntity be, Axis axis, float angle, int light) {
    buffer.light(light);
    buffer.rotateCentered(angle, Direction.get(AxisDirection.POSITIVE, axis));
    // 调试模式：network ID 颜色；过载：红/绿混合；正常：白色
    ...
}
```

- **`rotateCentered(angle, axis)`**: 以方块中心为原点绕轴旋转，实现旋转动画
- **调试颜色**: `KineticDebugger.isActive()` 时为 network ID 伪彩色
- **过载效果**: `overStressedEffect > 0` 红色，`< 0` 绿色

### 辅助方法

- **`shaft(Axis)`**: 返回 `AllBlocks.SHAFT` 默认状态设置指定轴
- **`getRotationAxisOf(be)`**: 取 `IRotate.getRotationAxis()` 的便利方法
- **`renderRotatingKineticBlock()` / `renderRotatingBuffer()`**: 静态工具方法

---

## CachedBuffers API

CachedBuffers 对 SuperByteBuffer 做缓存，避免每帧重建模型。

| 方法 | 用途 | 坐标变换 |
|---|---|---|
| `CachedBuffers.block(compartment, state)` | 完整 block 模型 | 无额外变换，由 blockstate rotation 处理 |
| `CachedBuffers.partialFacing(partial, state, direction)` | PartialModel + facing | z=16 → facing 方向 |
| `CachedBuffers.partialFacingVertical(partial, state, direction)` | 垂直朝向 partial | 同 partialFacing 但处理垂直轴 |
| `CachedBuffers.partial(partial, state)` | 无朝向变换 | 无 |

### compartment 用途

每个 cache compartment 是一个独立的缓存域，用 `static final` 字段定义：
```java
public static final SuperByteBufferCache.Compartment<BlockState> KINETIC_BLOCK = new SuperByteBufferCache.Compartment<>();
```

缓存 key 可以是 BlockState、Direction 等，不同 compartment 隔离缓存，避免互相污染。

### partialFacing 的坐标变换规则

- `partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.NORTH)`:
  - SHAFT_HALF 模型定义在 z 轴上（from z=8, to z=16）
  - `partialFacing` 将其旋转到指定 direction
  - 内部处理 UV 旋转以匹配面方向
- `partialFacingVertical` 在 `partialFacing` 基础上额外处理 up/down 方向

---

## PartialModel 系统

### 定义

```
内容/AllPartialModels.java
```

```java
public static final PartialModel SHAFT_HALF = block("shaft_half");
// 展开为:
// PartialModel.of(new ResourceLocation("create", "block/shaft_half"))
```

### 工厂方法

- `PartialModel.of(ResourceLocation)` — 核心工厂
- `AllPartialModels.block(name)` — 辅助方法，补全 `create:block/` 前缀

### 注册位置

所有 PartialModel 在 `AllPartialModels` 类中 `static final` 字段注册，类加载时自动初始化。
对应的 JSON 模型位于 `assets/create/models/block/` 下。

### 典型 PartialModel 示例

| PartialModel | 用途 | 对应文件 |
|---|---|---|
| `SHAFT_HALF` | 半截传动轴 | `models/block/shaft_half.json` |
| `SHAFTLESS_COGWHEEL` | 无轴齿轮 | `models/block/cogwheel_shaftless.json` |
| `COGWHEEL_SHAFT` | 齿轮的半截轴 | `models/block/cogwheel_shaft.json` |
| `DRILL_HEAD` | 钻头 | `models/block/mechanical_drill/head.json` |
| `BEARING_TOP` | 轴承面 | `models/block/bearing/top.json` |

---

## 标准渲染模式对照

| 模式 | getRotatedModel 返回 | renderSafe 附加 | 示例类 |
|---|---|---|---|
| 简易（仅 shaft） | — | SHAFT_HALF 渲染 | CreativeMotorRenderer |
| 经典（shaftless + shaft） | shaftless partial | 遍历方向渲染 SHAFT_HALF | EncasedCogRenderer |
| 大齿轮加轴 | shaftless partial + shaft | 独立 shaft 角度（含 22.5° 偏移） | BracketedKineticBlockEntityRenderer |
| 默认（全 block） | block 模型 | 无 | KineticBlockEntityRenderer |
| 自定义部分 | 自定义 partial | 自定义渲染逻辑 | DrillRenderer |

### EncasedCogRenderer 经典模式

```
内容/simpleRelays/encased/EncasedCogRenderer.java
```

```java
@Override
protected SuperByteBuffer getRotatedModel(SimpleKineticBlockEntity be, BlockState state) {
    return CachedBuffers.partialFacingVertical(
        large ? SHAFTLESS_LARGE_COGWHEEL : SHAFTLESS_COGWHEEL, state,
        Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE));
}

@Override
protected void renderSafe(...) {
    super.renderSafe(be, ...); // 渲染 shaftless cog
    if (VisualizationManager.supportsVisualization(be.getLevel())) return;
    // 遍历方向，对 hasShaftTowards 的面渲染 SHAFT_HALF
    for (Direction d : Iterate.directionsInAxis(axis)) {
        if (!def.hasShaftTowards(...)) continue;
        SuperByteBuffer shaft = CachedBuffers.partialFacing(SHAFT_HALF, state, d);
        kineticRotationTransform(shaft, be, axis, angle, light);
        shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }
}
```

### BracketedKineticBlockEntityRenderer 大齿轮模式

```
内容/simpleRelays/BracketedKineticBlockEntityRenderer.java
```

- 大齿轮 shaft 有额外的 22.5° 角度偏移（`getShaftAngleOffset` 根据位置奇偶性）
- 使用 `partialFacingVertical` + 独立的 `kineticRotationTransform`

---

## Flywheel Visual 系统

### KineticBlockEntityVisual

```
内容/kinetics/base/KineticBlockEntityVisual.java
```

- **基类**: `AbstractBlockEntityVisual<T>`（Flywheel API）
- **`rotationAxis()`**: 从 BlockState 的 `IRotate.getRotationAxis()` 获取
- **`rotationOffset()`**: 计算旋转偏移量：
  - 非轴方向坐标和偶数 → 22.5°（对齐齿）
  - 大齿轮 → 11.25°
  - 其他 → 0
- **`shouldOffset()`**: 基于位置奇偶性的对齐检测

### Flywheel 与 BESR 的 interplay

```
内容/kinetics/base/KineticBlockEntityRenderer.java:50
```

```
VisualizationManager.supportsVisualization(level) == true  → BESR 跳过，Flywheel 接管
VisualizationManager.supportsVisualization(level) == false → BESR 回退渲染
```

- Flywheel visual 类（如 `KineticBlockEntityVisual` 的子类）**不调用** `getRotatedModel()`
- 自定义 PartialModel 需注册对应的 Visual 类才能被 Flywheel 渲染
- BE 的 `requestModelDataUpdate()` 同时触发 BESR 和 Flywheel 的更新

### ⚠️ 关键陷阱：BESR 不要盲目跳过（无 Flywheel Visual 时）

**问题描述**：在自定义 BESR 中写 `if (VisualizationManager.supportsVisualization(level)) return;` 但**没有注册对应的 Flywheel Visual**，会导致 PartialModel 永远不渲染。

**根因**：`supportsVisualization(level)` 只检查 Flywheel 是否全局启用，**不检查当前 BE 是否有 Visual 注册**。

| 条件 | `supportsVisualization` | BESR | Flywheel | 结果 |
|---|---|---|---|---|
| 有 Visual + guard | true | 跳过 | Visual 渲染 | ✅ |
| 无 Visual + guard | true | 跳过 | 无 Visual | ❌ 永远空白 |
| 无 Visual + 无 guard | true | 继续渲染 | 无 Visual | ✅ |

**规则**：
1. 如果你没有写 Flywheel Visual 类——**不要在 BESR 中加 `supportsVisualization` 检查**
2. `KineticBlockEntityRenderer.renderSafe()` 基类已自带此检查（仅用于基类的 `getRenderedBlockState()` 渲染），子类叠加额外 PartialModel 时不应重复
3. Create 原版 `EncasedCogRenderer` 等虽然也加了这个检查，因为**它们有对应的 Flywheel Visual**——不要盲目模仿

**反例（本项目已修复）**：
```java
// ❌ 错误：RemoteEngine 没有 Flywheel Visual，这个 guard 导致 SHAFT_HALF 永远不渲染
if (VisualizationManager.supportsVisualization(be.getLevel())) return;

// ✅ 正确：直接渲染，不要检查
SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, facing);
standardKineticRotationTransform(shaft, be, light);
shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
```

---

## Block Model 与纹理 UV 惯例

### JSON 模型结构

**shafl_half.json**（参考 `assets/create/models/block/shaft_half.json`）:
```json
{
  "parent": "block/block",
  "textures": {
    "0": "create:block/axis",
    "1": "create:block/axis_top",
    "particle": "create:block/axis"
  },
  "elements": [{
    "from": [6, 6, 8], "to": [10, 10, 16],
    "faces": {
      "north": {"uv": [6, 6, 10, 10], "rotation": 180, "texture": "#1"},
      "east":  {"uv": [6, 0, 10, 8], "rotation": 270, "texture": "#0"},
      ...
    }
  }]
}
```

- **`parent: "block/block"`** 是所有 Create 自定义模型的标配
- **#0, #1** 引用 textures 中定义的变量

### UV 坐标约定

| face | u 方向 | v 方向 |
|---|---|---|
| north (+Z) | +X (右) | +Y (下) |
| south (-Z) | -X (左) | +Y (下) |
| east (+X) | -Z (后) | +Y (下) |
| west (-X) | +Z (前) | +Y (下) |
| up (+Y) | +X (右) | -Z (后) |
| down (-Y) | +X (右) | +Z (前) |

- UV 范围: `[0, 0]` (纹理左上) 到 `[texture_w, texture_h]` (纹理右下)，单位像素
- 默认纹理尺寸 16×16，除非指定 `texture_size`
- `uv` 四个值: `[u1, v1, u2, v2]`

### 元素不重叠设计

Creative Motor 模型（`assets/create/models/block/creative_motor/block.json`）展示了如何用多个 element 拼合：

1. **背板**: `[3,3,0] → [13,13,2]` 和 `[3,3,12] → [13,13,14]`
2. **中间结构**: `[5,5,9] → [11,11,13]` 等
3. **轴柱**: 四角 `[12,12,0] → [14,14,15]` 等
4. **前面板**: `[1,0,3] → [15,3,10]`

技巧：element 间边界精确相邻（如 0.1/2/2.1），不重叠，避免 z-fighting

### blockstate 旋转的视觉影响

**creative_motor blockstate**（`generated/resources/assets/create/blockstates/creative_motor.json`）:

```json
"variants": {
  "facing=north": { "model": ".../block", "y": 180 },
  "facing=east":  { "model": ".../block", "y": 270 },
  "facing=up":    { "model": ".../block_vertical" },
  "facing=down":  { "model": ".../block_vertical", "x": 180 }
}
```

- `x/y` 旋转在模型加载时应用，旋转整个模型的坐标空间
- `facing=north, y=180`: 模型绕 Y 轴 180°，原先朝南的 face 变为朝北
- `facing=up`: 使用单独 `block_vertical.json`，或可通过 `x=270` 实现
- **blockstate 旋转 vs partial 旋转**:
  - blockstate x/y → 整个块模型旋转（应用于 CachedBuffers.block 的外部）
  - partialFacing → CachedBuffers 内部的局部旋转变换，不影响 cached 的 compartment
