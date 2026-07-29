# Kinetic 运行时生命周期

## 目录

- [KineticBlockEntity 核心机制](#kineticblockentity-核心机制)
- [GeneratingKineticBlockEntity 发电机](#generatingkineticblockentity-发电机)
- [RotationPropagator 旋转传播](#rotationpropagator-旋转传播)
- [KineticNetwork 应力网络](#kineticnetwork-应力网络)
- [DirectionalKineticBlock 定向方块](#directionalkineticblock-定向方块)
- [Config 配置系统](#config-配置系统)

---

## KineticBlockEntity 核心机制

### tick() 执行流程

```
内容/kinetics/base/KineticBlockEntity.java:95
```

- **`tick()`**: 每 tick 调用。先检查 `!level.isClientSide && needsSpeedUpdate()` → `attachKinetics()`（触发 RotationPropagator）。调用 `super.tick()`（SmartBlockEntity → 初始化+behaviours tick），然后 `effects.tick()`。Client 端走 `tickAudio()` 后 return。Server 端每 `kineticValidationFrequency` ticks 执行 `validateKinetics()`。

- **`validateKinetics()`**: 检查 `hasSource()` 的 BE 的 source 是否仍然有效（source BE 存在且 speed ≠ 0）。无效则 `removeSource()` + `detachKinetics()`。

### speed 管理

```
内容/kinetics/base/KineticBlockEntity.java:285-297
```

| 方法 | 返回值 | 说明 |
|---|---|---|
| `getSpeed()` | `overStressed ? 0 : getTheoreticalSpeed()` | 对外暴露的真实转速，过载时为 0 |
| `getTheoreticalSpeed()` | `speed` 字段 | 理论转速，不受过载影响 |
| `getGeneratedSpeed()` | 0（基类） | 仅 GeneratingKineticBlockEntity 有意义 |
| `setSpeed(float)` | — | 直接写 `this.speed` 字段 |
| `setSource(BlockPos)` | — | 设置 source 并同步 network ID |

- **关键规则**: `getSpeed()` 在 `overStressed` 时返回 0，但内部 `speed` 字段保留原值
- **`overStressed` 设置位置**: `updateFromNetwork()` 中判断 `maxStress < currentStress && StressImpact.isEnabled()`
- **`onSpeedChanged(float prevSpeed)`**: 检测 fromOrToZero 和 directionSwap，触发 flickerTally

### stress 管理

```
内容/kinetics/base/KineticBlockEntity.java:176-186
```

- **`calculateStressApplied()`**: 通过 `BlockStressValues.getImpact()` 获取应力消耗基数（数据驱动）
  - 返回 `lastStressApplied`
  - 实际消耗 = `stress * |theoreticalSpeed|`
- **`calculateAddedStressCapacity()`**: 通过 `BlockStressValues.getCapacity()` 获取应力容量基数
  - 仅 GeneratingKineticBlockEntity 有意义
  - 实际容量 = `capacity * |generatedSpeed|`
- **`updateFromNetwork()`**: 由 KineticNetwork 调用，更新 `capacity`/`stress`/`networkSize`/`overStressed`
- **`getNetworkCapacity()` / `getNetworkStress()`**: 来自 network 的 `updateFromNetwork` 调用

### NBT 序列化

```
内容/kinetics/base/KineticBlockEntity.java:207-275
```

- **`write(CompoundTag, boolean clientPacket)`**:
  - 写入 `Speed`、`Source`、`Network`（含 Id/Stress/Capacity/Size/AddedStress/AddedCapacity）
  - 写入 `Sequence`（如有）
  - 标记 `NeedsSpeedUpdate`
- **`read(CompoundTag, boolean clientPacket)`**:
  - **关键**: 如果 `wasMoved` 为 true，跳过所有 kinetic 信息读取！
  - 正常读取：`Speed` → `source` → `Network` → 恢复 `network/capacity/stress/size`
  - client packet 时：检测 overStressed 变化触发特效，调用 `VisualizationHelper.queueUpdate`
- **`initialize()`**: world load / block place 时调用。如果有 network 则调用 `network.initFromTE()` 初始化。**不自动触发 `updateGeneratedRotation()`**——需手动调用
- **`warnOfMovement()`**: 设置 `wasMoved = true`，下次 read 时丢弃所有 kinetic 信息（装置搬运后）

### 旋转传播与自定义连接

```
内容/kinetics/base/KineticBlockEntity.java:522-584
```

- **`propagateRotationTo()`**: 返回 0 表示使用默认规则。返回非 0 值自定义转速比
- **`isCustomConnection()`**: 返回 true 表明存在非标准连接（非轴/非齿轮）
- **`addPropagationLocations()`**: 添加对角线邻居位置（小齿轮齿合对角齿轮）
- **`canPropagateDiagonally()`**: 默认仅小齿轮返回 true

### 静态工具方法

```
内容/kinetics/base/KineticBlockEntity.java:377-516
```

- **`switchToBlockState()`**: 保留 kinetic BE 数据切换 blockstate。检测 kinetic 等价性，不等价则 detach+removeSource
- **`convertToDirection(float, Direction)`**: 根据方向轴正负调整符号
- **`convertToAngular(float)`**: rpm → deg/tick = `speed * 360 / 60 / 20`
- **`convertToLinear(float)`**: rpm → blocks/tick = `speed / 512`

---

## GeneratingKineticBlockEntity 发电机

```
内容/kinetics/base/GeneratingKineticBlockEntity.java
```

### tick() 差异

- 在 `super.tick()` 之后检查 `reActivateSource`——为 true 时调用 `updateGeneratedRotation()` 并清空标记
- `reActivateSource` 在 `KineticBlockEntity.switchToBlockState()` 中被设为 true

### updateGeneratedRotation() 完整流程

```
内容/kinetics/base/GeneratingKineticBlockEntity.java:89-116
```

1. 获取 `getGeneratedSpeed()` 与 `prevSpeed` 比较
2. 如果速度变化 → `applyNewSpeed(prevSpeed, speed)`
3. 如果有 network 且 speed ≠ 0 → 更新 capacity + stress → `network.updateStress()`
4. 调用 `onSpeedChanged(prevSpeed)` → `sendData()`

### applyNewSpeed() 状态机

```
内容/kinetics/base/GeneratingKineticBlockEntity.java:118-164
```

| 条件 | 行为 |
|---|---|
| `speed == 0 && hasSource()` | 通知 capacity=0，保留 network 作为消费者 |
| `speed == 0 && !hasSource()` | `detachKinetics()` → `setSpeed(0)` → `setNetwork(null)` |
| `prevSpeed == 0` | 创建新 network → `attachKinetics()` |
| `hasSource() && \|prevSpeed\| >= \|speed\|` | 被压制，不操作（方向相反则爆 block） |
| `hasSource() && \|prevSpeed\| < \|speed\|` | 反超 → 成为新 source |
| `!hasSource()` | 重新 attach（reapply） |

### stress 相关

- **`notifyStressCapacityChange(float)`**: 更新 network 中的 capacity 记录
- **`addToGoggleTooltip()`**: 显示 generator stats（capacity provided），速度不同时按比例缩放

### ⚠️ 关键陷阱：notifyStressCapacityChange 必须在 network != null 时调用

**问题描述**：在自定义 tick 中调用 `notifyStressCapacityChange(capacity)` 时，如果 BE 已从网络中脱离（如连接的应力接收器被破坏），`getOrCreateNetwork()` 可能返回 null 导致 NPE。

**根因**：`updateGeneratedRotation()` 内部触发 RotationPropagator 传播，可能导致 BE 的 `this.network` 被置空。随后立即调用 `notifyStressCapacityChange()` 触发 `getOrCreateNetwork()`，若新网络创建失败则返回 null。

**复现路径**：
```
破坏应力接收器 → next tick → tryRefreshRpm()
  → 检测到 capacity 变化
  → updateGeneratedRotation()  // 内部触发传播，network 被置空
  → notifyStressCapacityChange() // NPE：getOrCreateNetwork() 返回 null
```

**规则**：
1. 在非标准 tick 流程中调用 `notifyStressCapacityChange()` 前，务必先检查 `network != null`
2. 标准流程（`updateGeneratedRotation()` 内部）由 Create 保证 network 有效性，无需额外检查

**反例（本项目已修复）**：
```java
// ❌ 错误：network 可能为 null
if (!level.isClientSide) {
    notifyStressCapacityChange(cachedAvailableStress);
}

// ✅ 正确：先检查 network
if (!level.isClientSide) {
    if (network != null)
        notifyStressCapacityChange(cachedAvailableStress);
    sendBlockUpdated(...);
    setChanged();
}
```

### ⚠️ 关键陷阱：calculateAddedStressCapacity 返回的是基数，Create 会自动乘以 speed

**问题描述**：`calculateAddedStressCapacity()` 的返回值会被 Create 在 `updateGeneratedRotation()` 中自动乘以 `|speed|`（参见 `KineticNetwork.getActualCapacityOf()` 中的 `sources.get(be) * |generatedSpeed|`）。如果直接返回目标容量值，实际容量会膨胀 speed 倍。

**正确设计**：
```java
// ❌ 错误：返回目标容量，实际容量 = 目标 × speed（过大）
@Override public float calculateAddedStressCapacity() { return cachedAvailableStress; }

// ✅ 正确：返回基数，让 Create 的 speed 乘法算出目标容量
// 目标: 容量 = availableStress, speed = availableStress / ratio
// 基数 × speed = ratio × (available/ratio) = available
@Override public float calculateAddedStressCapacity() { return ratio; }
```

### ⚠️ 关键陷阱：跨方块应力追踪（consumedRemotely 模式）

**问题描述**：当一个方块（引擎）从另一个方块（读取器）的网络"消耗"应力时，单纯的读值会导致读取器永远显示未消耗的应力值。

**解决方案**：在读取器中添加 `consumedRemotely` 字段，引擎读取原始可用应力后写回消耗量。

```java
// 读取器
private float consumedRemotely;
public float getAvailableStress() { return Math.max(0, capacity - stress - consumedRemotely); }
public void setConsumedRemotely(float amount) { this.consumedRemotely = amount; }

// 引擎：读取原始值（避免 feedback loop），换算后写回
float rawAvailable = reader.getNetworkCapacity() - reader.getNetworkStress();
reader.setConsumedRemotely(rawAvailable);
```

**关键**：引擎必须读取 `capacity - stress`（原始值），不能用 `getAvailableStress()`（调整后的值），否则产生 feedback loop（引擎消耗 → 可用变少 → RPM 降低 → 消耗变少 → 可用恢复 → RPM 升高 → ...）。

---

## RotationPropagator 旋转传播

```
内容/kinetics/RotationPropagator.java
```

### 速度修正系数

`getRotationSpeedModifier(from, to)` 计算两个 kinetic BE 间的速度修正：

| 连接类型 | 修正系数 | 条件 |
|---|---|---|
| 轴-轴 (hasShaftTowards) | ±1 | 方向决定符号 |
| 小齿轮-小齿轮 | -1 | 同轴不行、相邻 |
| 小齿轮-大齿轮 | -2 / -0.5 | 垂直连接 |
| 大齿轮-大齿轮 | ±1 | 不同轴、双轴偏移 |
| 链传动 | ChainDriveBlock 决定 | — |
| 速度控制器 | SpeedControllerBlockEntity 决定 | — |
| 自定义 | `propagateRotationTo()` 返回值 | — |

### propagateNewSource() 传播算法

```
内容/kinetics/RotationPropagator.java:219-300
```

- 遍历所有 connected neighbours
- 比较 `newSpeed` vs `speedOfNeighbour`
- **不兼容方向** → `world.destroyBlock(pos, true)`
- **超速** (maxRotationSpeed) → `world.destroyBlock(pos, true)`
- **flickerScore > 128** → `world.destroyBlock(pos, true)`
- **邻居更快** → 当前被压制，设 source 为邻居
- **当前更快** → 压制邻居，设邻居 source 为当前
- 递归 `propagateNewSource` 传播整个网络

### isConnected() 判断

```
内容/kinetics/RotationPropagator.java:402
```

连接判定：`isLargeCogToSpeedController || getRotationSpeedModifier != 0 || isCustomConnection`

### handleRemoved() 移除传播

```
内容/kinetics/RotationPropagator.java:309
```

- 遍历 neighbors，对 source 指向移除 BE 的 neighbor 执行 `propagateMissingSource()`
- `propagateMissingSource()` 清除整个子网络，寻找新 source

---

## KineticNetwork 应力网络

```
内容/kinetics/KineticNetwork.java
```

### 数据结构

| 字段 | 类型 | 用途 |
|---|---|---|
| `sources` | `Map<KineticBlockEntity, Float>` | 发电机→capacity |
| `members` | `Map<KineticBlockEntity, Float>` | 消费者→stress impact |
| `currentCapacity` / `currentStress` | float | 合计值 |
| `unloadedCapacity` / `unloadedStress` / `unloadedMembers` | | 未加载区块的缓存值 |

### 计算规则

- **`getActualCapacityOf(be)`**: `sources.get(be) * |generatedSpeed|`
- **`getActualStressOf(be)`**: `members.get(be) * |theoreticalSpeed|`
- **`calculateCapacity()`**: `sum(getActualCapacityOf) + unloadedCapacity`
- **`calculateStress()`**: `sum(getActualStressOf) + unloadedStress`
- 容量/应力都乘以转速绝对值，因此高速运转时应力消耗翻倍

### 关键方法

| 方法 | 触发时机 | 行为 |
|---|---|---|
| `initFromTE()` | `initialize()` | 从 NBT 恢复的未加载数据初始化 |
| `addSilently()` | `initialize()` | 静默添加，用 lastCapacity/lastStress 抵消 unloaded 值 |
| `add()` | 新加入 | 重新计算并标记 `networkDirty` |
| `remove()` | BE removed | 移除并设 capacity/stress=0，空则删 network |
| `updateCapacityFor()` | 发电机 capacity 变化 | 更新 sources map → `updateCapacity()` |
| `updateStressFor()` | 消费者 stress 变化 | 更新 members map → `updateStress()` |
| `updateNetwork()` | `networkDirty` 时 | 重新计算并同步所有成员 |

---

## DirectionalKineticBlock 定向方块

```
内容/kinetics/base/DirectionalKineticBlock.java
```

### FACING 属性

- 使用 `BlockStateProperties.FACING`（6 方向）
- `getPreferredFacing()`: 邻接 `IRotate` 方块时自动对齐到其 `hasShaftTowards` 的面
- `getStateForPlacement()`: 优先对齐相邻传动，否则朝向玩家视线（潜行时反向）
- `rotate()` / `mirror()`: 标准旋转镜像

### hasShaftTowards 约定

```
内容/kinetics/base/KineticBlock.java:51
```

- 基类返回 false，子类覆盖指明 shaft 连接面
- BESR 中 `hasShaftTowards` 决定是否渲染 `SHAFT_HALF` partial（参见 EncasedCogRenderer）

#### ⚠️ 关键约定：传动杆在 FACING 的背面

Create 所有标准 `DirectionalKineticBlock` 子类都将传动杆放在 FACING 的**对面**：

| 方块 | `hasShaftTowards` | 传动杆位置 |
|---|---|---|
| `DrillBlock` | `face == FACING.getOpposite()` | 直面在钻头侧，杆在背面 |
| `EncasedFanBlock` | `face == FACING.getOpposite()` | 直面在扇叶侧，杆在背面 |
| `SawBlock` (水平) | `face == FACING.getOpposite()` | 直面在锯齿侧，杆在背面 |

**必须遵循**：
```java
// ✅ 正确：杆在 FACING 反面
return face == state.getValue(FACING).getOpposite();

// ❌ 错误：杆在 FACING 同面——与 renderer 冲突导致应力网络断裂
return face == state.getValue(FACING);
```

**例外**：如果自定义 renderer 把 `SHAFT_HALF` 渲染在 FACING 同侧，则必须与 renderer 保持一致。但这种情况极少，不推荐。

#### ⚠️ 关键陷阱：hasShaftTowards 必须与 Renderer 同步

`KineticBlockEntityRenderer`（如 `StressReaderRenderer`、`RemoteEngineRenderer`）在其 `renderSafe()` 中会沿 `FACING` 方向渲染 `AllPartialModels.SHAFT_HALF`（半轴）。如果 `hasShaftTowards` 判定在 `FACING.getOpposite()` 而视觉效果在 FACING，玩家看到轴但应力网络实际不连接。

**规则**：
1. 默认使用 `FACING.getOpposite()`（Create 标准）
2. 如需自定义轴面渲染位置，在 renderer 中用 `getRotatedModel()` 或 `renderSafe()` 覆盖半轴渲染的朝向
3. 修复历史 bug 时：必须先确认 renderer 的轴渲染面，再改 `hasShaftTowards`

#### ⚠️ 关键约定：碰撞箱使用 CASING_12PX

Create 标准 `DirectionalKineticBlock` 子类的碰撞箱不是完整方块：

```java
// ✅ 正确：使用 12 像素厚的 casing，沿 FACING 方向
return AllShapes.CASING_12PX.get(state.getValue(FACING));

// ❌ 错误：完整方块遮挡传动杆视觉
return Shapes.block();
```

`CASING_12PX` 是 16×16×12 的壳体（沿 FACING 方向厚 12 像素），用 `.forDirectional()` 生成了 6 方向的 `VoxelShaper`。Create 的 Drill、Saw、Deployer 等均使用此模式。

#### ⚠️ 关键陷阱：override setPlacedBy 必须调 super

```java
// ✅ 正确
@Override
public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                        @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);  // ← 必须
    // 自定义逻辑...
}

// ❌ 错误：跳过 super 导致 missing AdvancementBehaviour + queueRotationIndicators
```

`KineticBlock.setPlacedBy()` 做了两件重要的事：
1. `AdvancementBehaviour.setPlacedBy(level, pos, placer)` — 注册 Forge advancement 行为
2. `kbe.effects.queueRotationIndicators()` — 播放放置时的旋转指示粒子效果

跳过 `super` 会导致这两个功能丢失，且可能影响其他依赖 `AdvancementBehaviour` 的机制。

### 与 Axis 属性的区别

| 类型 | 属性 | 适用场景 |
|---|---|---|
| `DirectionalKineticBlock` | FACING (6-way) | 定向机器（电机、钻头、风扇） |
| `RotatedPillarKineticBlock` | AXIS (3-way) | 轴对称部件（齿轮、轴） |
| `HorizontalKineticBlock` | HORIZONTAL_FACING | 水平定向 |
| `DirectionalAxisKineticBlock` | FACING + AXIS | 既有方向又有轴选择 |

### blockstate 旋转视觉影响

- blockstate 的 x/y 旋转在模型加载时执行，**改变模型的坐标空间**
- 模型空间坐标变换示例：`facing=north, x=90` 时 model up → world north
- 与 `partialFacing` 的区别：前者是 blockstate 整体旋转，后者是 CachedBuffers 对 partial model 的局部变换

---

## Config 配置系统

### 入口

```
内容/infrastructure/config/AllConfigs.java
```

```java
AllConfigs.server().kinetics.*       // CKinetics
AllConfigs.client().*                // CClient
AllConfigs.common().*                // CCommon
```

### CKinetics 关键配置项

```
内容/infrastructure/config/CKinetics.java
```

| 配置项 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `disableStress` | bool | false | 关闭应力机制 |
| `maxRotationSpeed` | int | 256 | 最大转速上限 (rpm) |
| `kineticValidationFrequency` | int | 60 | ticks 间隔验证 kinetic source |
| `mediumSpeed` | float | 30 | "中等"转速阈值 |
| `fastSpeed` | float | 100 | "快速"转速阈值 |
| `mediumStressImpact` | float | 4 | "中等"应力影响阈值 |
| `highStressImpact` | float | 8 | "高等"应力影响阈值 |
| `mediumCapacity` | float | 256 | "中等"容量阈值 |
| `highCapacity` | float | 1024 | "高等"容量阈值 |
| `maxBeltLength` | int | 20 | 传送带最大长度 |
| `fanPushDistance` | int | 20 | 风机最大推送距离 |
| `fanRotationArgmax` | int | 256 | 风机满属性转速 |

### CStress 应力配置

```
内容/infrastructure/config/CStress.java
```

- 记录在 `CREATE_CONFIG/server/kinetics/stressValues.v2/` 下
- 通过 `BlockStressValues.IMPACTS.registerProvider(stress::getImpact)` 和 `CAPACITIES` 注册数据提供者
- **附属不可使用** `CStress.setImpact()` / `setCapacity()`（会抛 `IllegalStateException: Non-Create blocks`，参见 `assertFromCreate`）
- **`data/<modid>/stress_values/` JSON 文件不被 Create 读取**——该目录是 Create 自身 datagen 的输出目录，不是数据包加载入口

#### ⚠️ 正确的应力值注册方式（附属模组）

使用 `BlockStressValues.IMPACTS` / `CAPACITIES` 的 `SimpleRegistry` 在 `FMLCommonSetupEvent` 中注册：

```java
// ModBlocks.java
public static void registerStressValues(final FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
        // 消费者/读数器：注册应力影响值
        BlockStressValues.IMPACTS.register(STRESS_READER.get(), () -> 0.0);
        // 发生器：注册应力容量值（动态计算的覆盖 calculateAddedStressCapacity 则不需要）
        BlockStressValues.CAPACITIES.register(REMOTE_ENGINE.get(), () -> 0.0);
        BlockStressValues.IMPACTS.register(REMOTE_ENGINE.get(), () -> 0.0);
    });
}
```

**注册时机**：必须在 `FMLCommonSetupEvent.enqueueWork()` 中执行，因为 `BlockStressValues` 的 `SimpleRegistry` 不是线程安全的，而 `enqueueWork` 保证在工作线程串行执行。

**适用场景**：

| 注册目标 | 方法 | 说明 |
|---------|------|------|
| 消费者应力影响 | `IMPACTS.register(block, () -> value)` | 方块每秒消耗 `\|speed\| × value` SU |
| 发生器应力容量 | `CAPACITIES.register(block, () -> value)` | 方块每秒提供 `\|speed\| × value` SU（被 `calculateAddedStressCapacity` 覆盖则无效） |
| 发生器 RPM 提示 | `RPM.register(block, new GeneratedRpm(n, mayGenLess))` | 仅 JEI 工具提示，运行时由 `getGeneratedSpeed()` 决定 |
