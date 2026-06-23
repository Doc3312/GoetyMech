---
name: "create-dev"
description: "Create 模组附属开发指南。涵盖依赖配置、Kinetic 运行时生命周期、ProcessingRecipe/Ponder API、Registrate 注册、BESR 渲染管线与 Flywheel。当需要开发 Create 机器、动力学方块、配方系统时调用本 skill。"
---

# Create 模组开发指南

## 依赖配置

```groovy
repositories {
    maven { url = "https://maven.createmod.net" }
    maven { url = "https://maven.ithundxr.dev/mirror" }
}

dependencies {
    implementation(fg.deobf("com.simibubi.create:create-${minecraft_version}:${create_version}:slim") { transitive = false })
    implementation(fg.deobf("net.createmod.ponder:Ponder-Forge-${minecraft_version}:${ponder_version}"))
    compileOnly(fg.deobf("dev.engine-room.flywheel:flywheel-forge-api-${minecraft_version}:${flywheel_version}"))
    runtimeOnly(fg.deobf("dev.engine-room.flywheel:flywheel-forge-${minecraft_version}:${flywheel_version}"))
    implementation(fg.deobf("com.tterrag.registrate:Registrate:${registrate_version}"))
}
```

```properties
minecraft_version = 1.20.1
create_version = 6.0.8-289
ponder_version = 1.0.91
flywheel_version = 1.0.5
registrate_version = MC1.20-1.3.3
```

---

## Kinetic 运行时生命周期

### KineticBlockEntity 核心机制

#### tick() 执行流程
- `tick()`: 每 tick 调用。先检查 `!level.isClientSide && needsSpeedUpdate()` → `attachKinetics()`。调用 `super.tick()`，然后 `effects.tick()`。Client 端走 `tickAudio()` 后 return。Server 端每 `kineticValidationFrequency` ticks 执行 `validateKinetics()`。
- `validateKinetics()`: 检查 `hasSource()` 的 BE 的 source 是否仍然有效。无效则 `removeSource()` + `detachKinetics()`。

#### speed 管理
| 方法 | 返回值 | 说明 |
|---|---|---|
| `getSpeed()` | `overStressed ? 0 : getTheoreticalSpeed()` | 对外暴露的真实转速，过载时为 0 |
| `getTheoreticalSpeed()` | `speed` 字段 | 理论转速，不受过载影响 |
| `getGeneratedSpeed()` | 0（基类） | 仅 GeneratingKineticBlockEntity 有意义 |
| `setSpeed(float)` | — | 直接写 `this.speed` 字段 |

**关键规则**: `getSpeed()` 在 `overStressed` 时返回 0，但内部 `speed` 字段保留原值。

#### stress 管理
- `calculateStressApplied()`: 通过 `BlockStressValues.getImpact()` 获取应力消耗基数（数据驱动）
- `calculateAddedStressCapacity()`: 通过 `BlockStressValues.getCapacity()` 获取应力容量基数
- `updateFromNetwork()`: 由 KineticNetwork 调用，更新 `capacity`/`stress`/`networkSize`/`overStressed`

### GeneratingKineticBlockEntity 发电机

#### updateGeneratedRotation() 完整流程
1. 获取 `getGeneratedSpeed()` 与 `prevSpeed` 比较
2. 如果速度变化 → `applyNewSpeed(prevSpeed, speed)`
3. 如果有 network 且 speed ≠ 0 → 更新 capacity + stress → `network.updateStress()`
4. 调用 `onSpeedChanged(prevSpeed)` → `sendData()`

### 关键陷阱

#### notifyStressCapacityChange 必须在 network != null 时调用
在自定义 tick 中调用 `notifyStressCapacityChange(capacity)` 时，如果 BE 已从网络中脱离，`getOrCreateNetwork()` 可能返回 null 导致 NPE。

```java
// ❌ 错误
notifyStressCapacityChange(cachedAvailableStress);

// ✅ 正确
if (network != null)
    notifyStressCapacityChange(cachedAvailableStress);
```

#### calculateAddedStressCapacity 返回的是基数，Create 会自动乘以 speed
`calculateAddedStressCapacity()` 的返回值会被 Create 在 `updateGeneratedRotation()` 中自动乘以 `|speed|`。

```java
// ❌ 错误：返回目标容量，实际容量 = 目标 × speed（过大）
@Override public float calculateAddedStressCapacity() { return cachedAvailableStress; }

// ✅ 正确：返回基数，让 Create 的 speed 乘法算出目标容量
@Override public float calculateAddedStressCapacity() { return ratio; }
```

#### 跨方块应力追踪（consumedRemotely 模式）
当一个方块从另一个方块的网络"消耗"应力时，单纯的读值会导致读取器永远显示未消耗的应力值。

解决方案：引擎必须读取 `capacity - stress`（原始值），不能用 `getAvailableStress()`（调整后的值），否则产生 feedback loop。

---

## ProcessingRecipe + Ponder + API

### ProcessingRecipe 配方系统
```java
public class MyProcessingRecipe extends ProcessingRecipe<MyProcessingRecipe> {
    public MyProcessingRecipe(ProcessingRecipeParams params) {
        super(MyRecipeTypes.MY_TYPE, params);
    }
    @Override protected int getMaxInputCount()       { return 2; }
    @Override protected int getMaxOutputCount()      { return 2; }
    @Override protected int getMaxFluidInputCount()  { return 0; }
    @Override protected int getMaxFluidOutputCount() { return 0; }
}
```

### Serializer
```java
public static final ProcessingRecipeSerializer<MyProcessingRecipe> SERIALIZER =
        new ProcessingRecipeSerializer<>(MyProcessingRecipe::new);
public static final RecipeType<MyProcessingRecipe> TYPE = RecipeType.simple(
        new ResourceLocation(MOD_ID, "my_processing"));
```

---

## Tags 系统 + 内容注册 + DataGen + Stress

### Tags 系统
Create 注册了大量 Tag，附属可通过数据包或 CreateRegistrate 加入。

| Tag | 说明 |
|---|---|
| `create:brittle` | 连接多方的脆弱方块 |
| `create:non_movable` | 装置无法移动 |
| `create:non_breakable` | 钻头/锯片无法破坏 |
| `create:passive_boiler_heaters` | 锅炉被动热源 |
| `create:windmill_sails` | 风帆有效方块 |
| `create:wrench_pickup` | 扳手快速捡起 |
| `create:fan_processing_catalysts/blasting` | 鼓风熔炼催化剂 |
| `create:fan_processing_catalysts/haunting` | 鼓风附魔催化剂 |
| `create:fan_processing_catalysts/smoking` | 鼓风烟熏催化剂 |
| `create:fan_processing_catalysts/splashing` | 鼓风洗涤催化剂 |

### 内容注册（CreateRegistrate）
不要使用 `Create.registrate()`（会检查包名）。

```java
public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
        .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
```

### 注册 Block
```java
public static final BlockEntry<MyMachineBlock> MY_MACHINE = REGISTRATE
        .block("my_machine", MyMachineBlock::new)
        .transform(BuilderTransformers.machine())
        .register();
```

### Stress 应力值
从 Create 6.0.0 起，应力值通过数据驱动定义：
`data/create/stress_values/` 下创建 JSON：
```json
{ "goetypunk:my_machine": 4.0, "goetypunk:my_generator": -256.0 }
```
正值 = 消耗，负值 = 产生。

---

## BESR 渲染管线与 Flywheel

### SafeBlockEntityRenderer
- `render()` 是 `final`——禁止子类 override！
- 改为实现 `renderSafe()` 抽象方法
- `render()` 先检查 `isInvalid(be)`，无效直接 return

### KineticBlockEntityRenderer
标准渲染流程：
```java
protected void renderSafe(T be, float partialTicks, PoseStack ms,
    MultiBufferSource buffer, int light, int overlay) {
    if (VisualizationManager.supportsVisualization(be.getLevel())) return; // Flywheel 接管
    BlockState state = getRenderedBlockState(be);
    RenderType type = getRenderType(be, state);
    renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);
}
```

### CachedBuffers API
| 方法 | 用途 |
|---|---|
| `CachedBuffers.block(compartment, state)` | 完整 block 模型 |
| `CachedBuffers.partialFacing(partial, state, direction)` | PartialModel + facing |
| `CachedBuffers.partialFacingVertical(partial, state, direction)` | 垂直朝向 partial |
| `CachedBuffers.partial(partial, state)` | 无朝向变换 |

### 标准渲染模式对照
| 模式 | getRotatedModel 返回 | 示例类 |
|---|---|---|
| 简易（仅 shaft） | — | CreativeMotorRenderer |
| 经典（shaftless + shaft） | shaftless partial | EncasedCogRenderer |
| 大齿轮加轴 | shaftless partial + shaft | BracketedKineticBlockEntityRenderer |
| 默认（全 block） | block 模型 | KineticBlockEntityRenderer |
| 自定义部分 | 自定义 partial | DrillRenderer |