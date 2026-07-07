# Create Mod API 参考文档 (Forge 1.20.1 / Create 6.0.8)

> 基于 `com.simibubi.create.api` 包及 foundation 层源码整理。
> 源路径: `D:\Libcode\Create-mc1.20.1-dev`

---

## 目录

1. [核心注册系统](#1-核心注册系统)
2. [Behaviour API](#2-behaviour-api)
3. [Contraption API](#3-contraption-api)
4. [DataGen / Recipe API](#4-datagen--recipe-api)
5. [Equipment API](#5-equipment-api)
6. [Effect API](#6-effect-api)
7. [Event API](#7-event-api)
8. [Packager API](#8-packager-api)
9. [Schematic API](#9-schematic-api)
10. [Stress API](#10-stress-api)
11. [Foundation 基类](#11-foundation-基类)
12. [Impl 实现层](#12-impl-实现层)
13. [Content 层关键基类与接口](#13-content-层关键基类与接口)

---

## 1. 核心注册系统

### 1.1 `SimpleRegistry<K, V>` — 轻量注册表

`com.simibubi.create.api.registry.SimpleRegistry`

线程安全，支持直接注册 + lazy provider 两种模式。

| 方法 | 说明 |
|------|------|
| `register(K object, V value)` | 直接注册 key→value，冲突会抛异常 |
| `registerProvider(Provider<K,V> provider)` | 添加 fallback provider（reverse-order 查询） |
| `invalidate()` | 使所有 provider 缓存失效 |
| `get(K object)` | 查询（先查直接注册，再查 provider） |
| `get(StateHolder<K,?> state)` | 从 BlockState/FluidState 等快捷查询 |

**`Provider` 接口：**

| 方法 | 说明 |
|------|------|
| `get(K object)` | 返回 null 则跳过 |
| `onRegister(Runnable invalidate)` | **注册后的回调** — 参数 `invalidate` 是一个 `Runnable`，调用时清除此 provider 的缓存。常用于监听 TagReloadEvent，在 tag 变更时自动失效 |
| `forTag(TagKey<K>, Function<K,Holder<K>>, V)` | 为整个 tag 提供值（自动注册 tag reload 监听，调用 `invalidate.run()` 刷新） |
| `forBlockTag(...)`/`forItemTag(...)`/`forEntityTag(...)`/`forFluidTag(...)` | 各类型的快捷方法 |

**Provider 执行机制：**
1. `get(K)` 先查直接注册 → 有值直接返回
2. 无直接注册 → 遍历所有 provider（reverse-registration order，后注册的先查）
3. provider 返回值被 **缓存**（除非调用 `invalidate()`）
4. 常用于 tag-based 注册（如 `create:passive_boiler_heaters` tag 下的所有方块自动获得 `BoilerHeater.PASSIVE`）

**`SimpleRegistry.Multi<K, V>`** — 支持多个值 per key：

| 方法 | 说明 |
|------|------|
| `add(K, V)` | 追加一个值 |
| `addProvider(Provider<K,V>)` | 追加 provider |
| `get(K)` / `get(StateHolder)` | 返回 `List<V>`，永不返回 null |

---

### 1.2 `CreateRegistries` — 注册键

`com.simibubi.create.api.registry.CreateRegistries`

Create 定义的所有 `ResourceKey<Registry<T>>` 常量：

| 键 | 值类型 |
|----|--------|
| `ARM_INTERACTION_POINT_TYPE` | `ArmInteractionPointType` |
| `FAN_PROCESSING_TYPE` | `FanProcessingType` |
| `ITEM_ATTRIBUTE_TYPE` | `ItemAttributeType` |
| `DISPLAY_SOURCE` | `DisplaySource` |
| `DISPLAY_TARGET` | `DisplayTarget` |
| `MOUNTED_ITEM_STORAGE_TYPE` | `MountedItemStorageType<?>` |
| `MOUNTED_FLUID_STORAGE_TYPE` | `MountedFluidStorageType<?>` |
| `CONTRAPTION_TYPE` | `ContraptionType` |
| `POTATO_PROJECTILE_TYPE` | `PotatoCannonProjectileType` |
| `POTATO_PROJECTILE_RENDER_MODE` | `Codec<? extends PotatoProjectileRenderMode>` |
| `POTATO_PROJECTILE_ENTITY_HIT_ACTION` | `Codec<? extends PotatoProjectileEntityHitAction>` |
| `POTATO_PROJECTILE_BLOCK_HIT_ACTION` | `Codec<? extends PotatoProjectileBlockHitAction>` |

### 1.3 `CreateBuiltInRegistries` — 实际 Registry 实例

`com.simibubi.create.api.registry.CreateBuiltInRegistries`

Create 通过 Mixin 注入到 `BuiltInRegistries` 的静态实例：

| 字段 | 类型 |
|------|------|
| `ARM_INTERACTION_POINT_TYPE` | `Registry<ArmInteractionPointType>` |
| `FAN_PROCESSING_TYPE` | `Registry<FanProcessingType>`（带 freeze callback） |
| `ITEM_ATTRIBUTE_TYPE` | `Registry<ItemAttributeType>` |
| `DISPLAY_SOURCE` | `Registry<DisplaySource>` |
| `DISPLAY_TARGET` | `Registry<DisplayTarget>` |
| `MOUNTED_ITEM_STORAGE_TYPE` | 带 intrusive holders |
| `MOUNTED_FLUID_STORAGE_TYPE` | `Registry<MountedFluidStorageType<?>>` |
| `CONTRAPTION_TYPE` | 带 intrusive holders |
| `POTATO_PROJECTILE_RENDER_MODE` | `Registry<Codec<?>>` |
| `POTATO_PROJECTILE_ENTITY_HIT_ACTION` | `Registry<Codec<?>>` |
| `POTATO_PROJECTILE_BLOCK_HIT_ACTION` | `Registry<Codec<?>>` |

### 1.4 `SimpleBuilder<R,T,P>` — Registrate 集成

`com.simibubi.create.api.registry.registrate.SimpleBuilder`

用于通过 Registrate 注册自定义值到 `SimpleRegistry`。链式调用：

```java
// 注册到 SimpleRegistry<Block, ?>:
.byBlock(registry)
// 注册到 SimpleRegistry.Multi<Block, ?>:
.byBlock(multiRegistry)
// 注册到 SimpleRegistry<BlockEntityType<?>, ?>:
.byBlockEntity(registry)
// 注册 tag 级关联:
.associateBlockTag(tagKey)
.associateBeTag(tagKey)
.associateEntityTag(tagKey)
.associateFluidTag(tagKey)
```

## 2. Behaviour API

### 2.1 `DisplaySource` — 显示链接数据源

`com.simibubi.create.api.behaviour.display.DisplaySource`

| 静态字段 | 类型 |
|----------|------|
| `BY_BLOCK` | `SimpleRegistry.Multi<Block, DisplaySource>` |
| `BY_BLOCK_ENTITY` | `SimpleRegistry.Multi<BlockEntityType<?>, DisplaySource>` |

关键方法：

| 方法 | 说明 |
|------|------|
| `provideText(context, stats)` | **抽象** — 返回文本行列表 |
| `transferData(context, activeTarget, line)` | 将数据写到目标 |
| `onSignalReset(context)` | 信号重置回调 |
| `populateData(context)` | 数据填充回调 |
| `getPassiveRefreshTicks()` | 被动刷新间隔（默认 100） |
| `shouldPassiveReset()` | 是否被动重置 |
| `loadFlapDisplayLayout(context, flapDisplay, layout, lineIndex)` | 翻牌显示布局加载 |
| `provideFlapDisplayText(context, stats)` | 翻牌显示文本提供 |
| `initConfigurationWidgets()` | 客户端配置界面 |

工具方法：

```java
// Registrate builder transformer:
DisplaySource.displaySource(sourceEntry)
// 查询：
DisplaySource.get(ResourceLocation id)
DisplaySource.getAll(LevelAccessor level, BlockPos pos)
```

---

### 2.2 `DisplayTarget` — 显示链接目标

`com.simibubi.create.api.behaviour.display.DisplayTarget`

| 静态字段 | 类型 |
|----------|------|
| `BY_BLOCK` | `SimpleRegistry<Block, DisplayTarget>` |
| `BY_BLOCK_ENTITY` | `SimpleRegistry<BlockEntityType<?>, DisplayTarget>` |

| 方法 | 说明 |
|------|------|
| `acceptText(line, text, context)` | **抽象** — 接受文本 |
| `provideStats(context)` | **抽象** — 提供 UI 统计信息 |
| `getMultiblockBounds(level, pos)` | 多块边界 |
| `getLineOptionText(line)` | 行选项文本 |
| `isReserved(line, target, context)` | 保留行检测 |
| `requiresComponentSanitization()` | 是否需要组件清洗 |

工具方法：

```java
DisplayTarget.displayTarget(entry)
DisplayTarget.get(ResourceLocation id)
DisplayTarget.get(LevelAccessor, BlockPos)
```

---

### 2.3 `MovingInteractionBehaviour` — 移动装置交互

`com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour`

| 静态字段 | 类型 |
|----------|------|
| `REGISTRY` | `SimpleRegistry<Block, MovingInteractionBehaviour>` |

| 方法 | 说明 |
|------|------|
| `handlePlayerInteraction(player, hand, localPos, contraptionEntity)` | 玩家交互处理 |
| `handleEntityCollision(entity, localPos, contraptionEntity)` | 实体碰撞处理 |
| `setContraptionActorData(...)` | 更新 actor 数据 |
| `setContraptionBlockData(...)` | 更新方块数据 |

工具方法：
```java
MovingInteractionBehaviour.interactionBehaviour(behaviour) // → NonNullConsumer<Block>
```

---

### 2.4 `ConductorBlockInteractionBehavior` — 列车管理员交互

`com.simibubi.create.api.behaviour.interaction.ConductorBlockInteractionBehavior`

继承自 `MovingInteractionBehaviour`，用于列车上充当管理员的方块（如烈焰炉）。

| 方法 | 说明 |
|------|------|
| `isValidConductor(state)` | **抽象** — 是否有效管理员 |
| `onScheduleUpdate(hasSchedule, state, blockStateSetter)` | 时刻表更新回调 |

内置实现：`BlazeBurner` — 烈焰炉管理员。

---

### 2.5 `MovementBehaviour` — 装置 Actor

`com.simibubi.create.api.behaviour.movement.MovementBehaviour`

| 静态字段 | 类型 |
|----------|------|
| `REGISTRY` | `SimpleRegistry<Block, MovementBehaviour>` |

| 方法 | 说明 |
|------|------|
| `isActive(context)` | 是否激活 |
| `tick(context)` | 每 tick 调用 |
| `startMoving(context)` | 开始移动 |
| `visitNewPosition(context, pos)` | 访问新位置 |
| `getActiveAreaOffset(context)` | 活动区域偏移 |
| `canBeDisabledVia(context)` | 能否被禁用 |
| `onDisabledByControls(context)` | 被禁用回调 |
| `mustTickWhileDisabled()` | 禁用时是否仍需 tick |
| `dropItem(context, stack)` | 丢出物品 |
| `onSpeedChanged(context, oldMotion, motion)` | 速度变化回调 |
| `stopMoving(context)` | 停止移动 |
| `cancelStall(context)` | 取消卡顿 |
| `writeExtraData(context)` | 写入额外 NBT |
| `disableBlockEntityRendering()` | 禁用 BlockEntity 渲染 |
| `renderInContraption(...)` | 客户端装置内渲染 |
| `createVisual(...)` | Flywheel Visual 工厂 |

工具方法：
```java
MovementBehaviour.movementBehaviour(behaviour)
```

---

### 2.6 `BlockSpoutingBehaviour` — 喷灌方块行为

`com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour`

`@FunctionalInterface`

| 静态字段 | 类型 |
|----------|------|
| `BY_BLOCK` | `SimpleRegistry<Block, BlockSpoutingBehaviour>` |
| `BY_BLOCK_ENTITY` | `SimpleRegistry<BlockEntityType<?>, BlockSpoutingBehaviour>` |

| 方法 | 说明 |
|------|------|
| `fillBlock(level, pos, spout, availableFluid, simulate)` | **抽象** — 填充方块，返回消耗的量；0 表示取消 |

```java
// 查询：
BlockSpoutingBehaviour.get(Level, BlockPos)
```

---

### 2.7 `CauldronSpoutingBehavior` — 炼药锅喷灌

`com.simibubi.create.api.behaviour.spouting.CauldronSpoutingBehavior`

| 静态字段 | 类型 |
|----------|------|
| `CAULDRON_INFO` | `SimpleRegistry<Fluid, CauldronInfo>` |

注册流体到 `CAULDRON_INFO` 即可让喷灌填充空炼药锅。

内置：Water→250mb→WaterCauldron, Lava→1000mb→LavaCauldron。

`CauldronInfo` record: `(int amount, BlockState cauldron)`

---

### 2.8 `StateChangingBehavior` — 状态变换喷灌

`com.simibubi.create.api.behaviour.spouting.StateChangingBehavior`

一个 `BlockSpoutingBehaviour` 的 record 实现，用于修改 BlockState：

| 构造参数 | 说明 |
|----------|------|
| `amount` | 每次消耗的流体量 |
| `fluidTest` | 匹配可用流体 |
| `canFill` | 匹配目标方块状态 |
| `fillFunction` | 转换函数 |

工厂方法：

```java
// 设置到指定状态:
StateChangingBehavior.setTo(amount, fluidTest, block/state)
// 递增 IntegerProperty:
StateChangingBehavior.incrementingState(amount, fluidTest, property)
```

---

### 2.9 `BoilerHeater` — 锅炉热源

`com.simibubi.create.api.boiler.BoilerHeater`

`@FunctionalInterface`，用于为锅炉提供热量。

| 静态常量 | 值 | 说明 |
|----------|-----|------|
| `PASSIVE_HEAT` | `0` | 被动热源标记（火焰、岩浆块） |
| `NO_HEAT` | `-1` | 无热量标记 |
| `PASSIVE` | 内置实例 | 用于 `create:passive_boiler_heaters` block tag |
| `BLAZE_BURNER` | 内置实例 | 烈焰炉热源——addon 可复用 |

| 静态字段 | 类型 |
|----------|------|
| `REGISTRY` | `SimpleRegistry<Block, BoilerHeater>` |

```java
// 查询：
float heat = BoilerHeater.findHeat(level, pos, state); // <0 表示无热量
```

`float getHeat(Level level, BlockPos pos, BlockState state)` — 返回值含义：
- `≥1` — 有效热量（越大越强）
- `0` (`PASSIVE_HEAT`) — 被动热（限制锅炉能力）
- `<0` (`NO_HEAT`) — 无热量

---

### 2.10 `ConnectivityHandler` — 多块结构连接管理器

`com.simibubi.create.api.connectivity.ConnectivityHandler`

用于管理 FluidTank / ItemVault 等多块方块结构的自动化连接。实现方需实现 `IMultiBlockEntityContainer`。

```java
// 形成多块：
ConnectivityHandler.formMulti(be); // T extends BlockEntity & IMultiBlockEntityContainer
// 拆分多块：
ConnectivityHandler.splitMulti(be);
// 查询指定位置的部件：
ConnectivityHandler.partAt(type, level, pos); // → T (nullable)
// 判断两个位置是否在同一多块中：
ConnectivityHandler.isConnected(level, pos, other); // → boolean
```

**`IMultiBlockEntityContainer` 所需方法：** `getController()`、`getControllerBE()`、`setController(BlockPos)`、`removeController(boolean)`、`getWidth()`/`setWidth(int)`、`getHeight()`/`setHeight(int)`、`getMaxWidth()`、`getMaxLength(axis, width)`、`getMainConnectionAxis()`、`getExtraData()`/`setExtraData(Object)`、`modifyExtraData(Object)`、`preventConnectivityUpdate()`、`notifyMultiUpdated()`。

子接口：
- `IMultiBlockEntityContainer.Fluid` — `hasTank()`, `getTank(int)`, `setTankSize(int, int)`, `getFluid(int)`, `getTankSize(int)`
- `IMultiBlockEntityContainer.Inventory` — `hasInventory()`

---

## 3. Contraption API

### 3.1 `BlockMovementChecks` — 方块移动检查

`com.simibubi.create.api.contraption.BlockMovementChecks`

5 种检查接口，每个可注册多个 check（不限制数量），按 reverse-registration 顺序查询。

| 注册方法 | 接口 | 说明 |
|----------|------|------|
| `registerMovementNecessaryCheck(check)` | `MovementNecessaryCheck` | 方块是否必须移动 |
| `registerMovementAllowedCheck(check)` | `MovementAllowedCheck` | 方块是否可移动 |
| `registerBrittleCheck(check)` | `BrittleCheck` | 是否需要支撑物 |
| `registerAttachedCheck(check)` | `AttachedCheck` | 是否附着于相邻方块 |
| `registerNotSupportiveCheck(check)` | `NotSupportiveCheck` | 是否不支撑相邻方块 |

查询方法：

| 查询 | 说明 |
|------|------|
| `isMovementNecessary(state, world, pos)` | 返回 `boolean` |
| `isMovementAllowed(state, world, pos)` | 返回 `boolean` |
| `isBrittle(state)` | 返回 `boolean` |
| `isBlockAttachedTowards(state, world, pos, direction)` | 返回 `boolean` |
| `isNotSupportive(state, facing)` | 返回 `boolean` |

`CheckResult` 枚举：`SUCCESS` / `FAIL` / `PASS`（PASS 表示不处理，交给下一个）

---

### 3.2 `ContraptionMovementSetting` — 移动设置

`com.simibubi.create.api.contraption.ContraptionMovementSetting`

| 枚举值 | 说明 |
|--------|------|
| `MOVABLE` | 完全可移动 |
| `NO_PICKUP` | 可移动但矿车装置不可拾取 |
| `UNMOVABLE` | 不可移动 |

```java
// 注册:
ContraptionMovementSetting.REGISTRY // SimpleRegistry<Block, Supplier<ContraptionMovementSetting>>
// 查询:
ContraptionMovementSetting.get(BlockState/Block)
// 通过 Block 实现接口:
MovementSettingProvider.getContraptionMovementSetting()
```

---

### 3.3 `ContraptionType` — 装置类型

`com.simibubi.create.api.contraption.ContraptionType`

内置包含 `factory` (Supplier) 和 `holder`，注册于 `CreateBuiltInRegistries.CONTRAPTION_TYPE`。

```java
new ContraptionType(() -> new YourContraption());
contraptionType.is(TagKey<ContraptionType> tag);
ContraptionType.fromType(String typeId);
```

---

### 3.4 `MountedDispenseBehavior` — 装置发射器

`com.simibubi.create.api.contraption.dispenser.MountedDispenseBehavior`

平行于 vanilla `DispenseItemBehavior`。

| 静态字段 | 说明 |
|----------|------|
| `REGISTRY` | `SimpleRegistry<Item, MountedDispenseBehavior>`（内置 `DispenserBehaviorConverter` fallback） |

| 方法 | 说明 |
|------|------|
| `dispense(stack, context, pos)` | 发射物品 |
| `getDispenserNormal(ctx)` | 计算发射方向 |
| `getClosestFacingDirection(vec)` | 方向向量→Direction |
| `placeItemInInventory(stack, context, pos)` | 尝试放回物品 |

内置实现：

| 类 | 说明 |
|----|------|
| `DefaultMountedDispenseBehavior` | 默认实现（丢出物品） |
| `MountedProjectileDispenseBehavior` | 投射物（如弓箭） |
| `OptionalMountedDispenseBehavior` | 可能失败的发射 |

---

### 3.5 安装式 Fluid Storage

`MountedFluidStorage` (`IFluidHandler`)

| 类 | 说明 |
|----|------|
| `MountedFluidStorage` | 抽象基类，含 `CODEC`、`type`、`unmount()` |
| `MountedFluidStorageType<T>` | 工厂类，`mount(level, state, pos, be)` → `T` |
| `MountedFluidStorageWrapper` | `CombinedTankWrapper` + `ImmutableMap<BlockPos, MountedFluidStorage>` |
| `WrapperMountedFluidStorage<T>` | 委托给 wrapped handler |

```java
// Registrate 注册:
MountedFluidStorageType.mountedFluidStorage(typeEntry)
```

### 3.6 安装式 Item Storage

`MountedItemStorage` (`IItemHandlerModifiable`)

| 类 | 说明 |
|----|------|
| `MountedItemStorage` | 抽象基类，含 `CODEC`、`type`、`unmount()`、`handleInteraction()` |
| `MountedItemStorageType<T>` | 工厂类，`mount(level, state, pos, be)` → `T`，支持 `is(TagKey)` |
| `MountedItemStorageWrapper` | `CombinedInvWrapper` + `ImmutableMap<BlockPos, MountedItemStorage>` |
| `WrapperMountedItemStorage<T>` | 委托给 wrapped handler |

内置实现：

| 类 | 说明 |
|----|------|
| `SimpleMountedStorage` | 通用实现，从 `IItemHandlerModifiable` 复制内容 |
| `SimpleMountedStorageType` | 通用工厂，含 `Impl` 内部类 |
| `ChestMountedStorage` | 双箱子 GUI 合并 |
| `ChestMountedStorageType` | 从 `Container` 获取 handler |

```java
// Registrate 注册:
MountedItemStorageType.mountedItemStorage(typeEntry)
```

Menu 支持：

```java
MountedStorageMenus.createGeneric(name, handler, stillValid, onClose) // 1-6 排 9 栏
MountedStorageMenus.createGeneric9x9(name, handler, stillValid, onClose) // 9 格
```

### 3.7 `SyncedMountedStorage` — 同步存储接口

`com.simibubi.create.api.contraption.storage.SyncedMountedStorage`

| 方法 | 说明 |
|------|------|
| `isDirty()` | 是否需要同步 |
| `markClean()` | 标记已同步 |
| `afterSync(contraption, localPos)` | 同步后客户端回调 |

---

### 3.8 `PortalTrackProvider` — 传送门轨道

`com.simibubi.create.api.contraption.train.PortalTrackProvider`

`@FunctionalInterface`，注册于 `SimpleRegistry<Block, PortalTrackProvider>`

```java
Exit findExit(ServerLevel level, BlockFace face);
static boolean isSupportedPortal(BlockState state);
static Exit getOtherSide(ServerLevel, BlockFace inboundTrack);
static Exit fromTeleporter(level, face, firstDim, secondDim, customPortalForcer);
static Exit fromProbe(level, face, firstDim, secondDim, portalInfoProvider);
```

`Exit` record: `(ServerLevel level, BlockFace face)`

---

### 3.9 Transformable Block

| 接口/类 | 方法 | 说明 |
|----------|------|------|
| `TransformableBlock` | `transform(state, transform)` | Block 变换 |
| `TransformableBlockEntity` | `transform(be, transform)` | BE 变换 |
| `MovedBlockTransformerRegistries.BLOCK_TRANSFORMERS` | `SimpleRegistry<Block, BlockTransformer>` | Block 变换注册表 |
| `MovedBlockTransformerRegistries.BLOCK_ENTITY_TRANSFORMERS` | `SimpleRegistry<BEType, BlockEntityTransformer>` | BE 变换注册表 |

---

## 4. DataGen / Recipe API

### 4.1 Recipe 类型总表

| 配方类型 | RecipeGen 基类 | 机器 |
|----------|---------------|------|
| Crushing | `CrushingRecipeGen` | Crushing Wheels |
| Milling | `MillingRecipeGen` | Millstone |
| Cutting | `CuttingRecipeGen` | Mechanical Saw |
| Washing / Splashing | `WashingRecipeGen` | Bulk Washing |
| Pressing | `PressingRecipeGen` | Mechanical Press |
| Mixing | `MixingRecipeGen` | Mechanical Mixer |
| Compacting | `CompactingRecipeGen` | Mechanical Press (basin) |
| Filling | `FillingRecipeGen` | Spout |
| Emptying | `EmptyingRecipeGen` | Spout (empty) |
| Deploying | `DeployingRecipeGen` | Deployer |
| Polishing | `PolishingRecipeGen` | Sand Paper |
| Haunting | `HauntingRecipeGen` | Soul Fire |
| Item Application | `ItemApplicationRecipeGen` | Deployer |
| Mechanical Crafting | `MechanicalCraftingRecipeGen` | Mechanical Crafter |
| Sequenced Assembly | `SequencedAssemblyRecipeGen` | Sequenced Assembly |

### 4.2 `ProcessingRecipeGen` 使用

继承 `ProcessingRecipeGen`，覆写 `getRecipeType()`：

```java
public abstract class YourRecipeGen extends ProcessingRecipeGen {
    public YourRecipeGen(PackOutput output, String modid) { super(output, modid); }

    @Override protected IRecipeTypeInfo getRecipeType() { return AllRecipeTypes.CRUSHING; }

    GeneratedRecipe MY_RECIPE = create("my_recipe", b -> b
        .require(Ingredient.of(Items.IRON_INGOT))
        .output(Items.GOLD_NUGGET)
        .duration(200));
}
```

`create()` 方法重载：

```java
// 单输入物品：
create(Supplier<ItemLike> ingredient, UnaryOperator<ProcessingRecipeBuilder<T>>)
// 自定义 ID + builder：
create(ResourceLocation id, UnaryOperator<ProcessingRecipeBuilder<T>>)
create(String name, UnaryOperator<ProcessingRecipeBuilder<T>>)
// 懒加载 ID：
createWithDeferredId(Supplier<ResourceLocation>, UnaryOperator<ProcessingRecipeBuilder<T>>)
// 指定 namespace + 单输入：
create(String namespace, Supplier<ItemLike>, UnaryOperator<ProcessingRecipeBuilder<T>>)
```

### 4.3 `ProcessingRecipeBuilder` 方法

```java
// 输入
.require(Ingredient / ItemLike / TagKey<Item> / DatagenMod / ResourceLocation / Fluid / FluidIngredient)
// 输出
.output(ItemLike) / .output(float chance, ItemLike, int amount)
.output(Fluid, int amount) / .output(FluidStack)
// 设置
.duration(int ticks) / .averageProcessingDuration()
.requiresHeat(HeatCondition) // NONE / HEATED / SUPERHEATED
.toolNotConsumed()
// 条件
.whenModLoaded(String modid) / .whenModMissing(String modid)
.withCondition(ICondition)
```

### 4.4 `MechanicalCraftingRecipeGen` 使用

```java
create(resultItem::get)
    .returns(1)
    .withSuffix("_alt")
    .recipe(b -> b
        .key('A', Items.IRON_INGOT)
        .patternLine("AAA")
        .patternLine("A A")
        .patternLine("AAA")
        .disallowMirrored()
        .whenModLoaded("other_mod"))
```

### 4.5 `SequencedAssemblyRecipeGen` 使用

```java
create("my_assembly", b -> b
    .require(Items.IRON_INGOT)
    .transitionTo(Items.IRON_BLOCK)
    .addStep(DeployerApplication.deploy(Items.DIAMOND), 200)
    .addStep(Pressing.press(), 100)
    .loops(5));
```

### 4.6 `CrushingRecipeGen` 辅助方法

```java
mineralRecycling(AllPaletteStoneTypes type, ...)
stoneOre(ore, raw, expectedAmount, duration)
deepslateOre(ore, raw, expectedAmount, duration)
netherOre(ore, raw, expectedAmount, duration)
moddedOre(CommonMetal, result)
moddedRawOre(metal, result) / moddedRawOreBlock(metal, result)
```

### 4.7 `CuttingRecipeGen` 辅助方法

```java
stripAndMakePlanks(wood, stripped, planks)
cuttingCompat(DatagenMod, String... woodtypes)
```

### 4.8 `DeployingRecipeGen` 辅助方法

```java
copperChain(CopperBlockSet)
addWax(waxed, nonWaxed)
oxidizationChain(List<Supplier<ItemLike>>)
```

### 4.9 `ItemApplicationRecipeGen` 辅助方法

```java
woodCasing(String type, Supplier<ItemLike> ingredient, Supplier<ItemLike> output)
woodCasingTag(String type, Supplier<TagKey<Item>>, output)
```

### 4.10 `FillingRecipeGen` 辅助方法

```java
moddedGrass(DatagenMod, String name)
```

### 4.11 `MixingRecipeGen` 辅助方法

```java
moddedMud(DatagenMod, String name)
```

### 4.12 `PressingRecipeGen` 辅助方法

```java
moddedCompacting(DatagenMod, String input, String output)
moddedPaths(DatagenMod, String... blocks)
```

### 4.13 `WashingRecipeGen` 辅助方法

```java
convert(Block, Block result)
crushedOre(crushed, nugget, secondary, secondaryChance)
```

### 4.14 `HauntingRecipeGen` 辅助方法

```java
convert(ItemLike input, ItemLike result)
moddedConversion(DatagenMod, String input, String output)
```

### 4.15 `MillingRecipeGen` 辅助方法

```java
moddedSandstone(DatagenMod, String name)
```

### 4.16 `TrainHatInfoProvider` — 列车帽子

`com.simibubi.create.api.data.TrainHatInfoProvider`

```java
makeInfoFor(EntityType<?>, Vec3 offset)
makeInfoFor(EntityType<?>, Vec3 offset, String part)
makeInfoFor(EntityType<?>, Vec3 offset, float scale)
makeInfoFor(EntityType<?>, Vec3 offset, String part, float scale)
makeInfoFor(EntityType<?>, Vec3 offset, String part, int cubeIndex, float scale)
```

### 4.17 `BaseRecipeProvider` — 通用配方 DataGen

`com.simibubi.create.api.data.recipe.BaseRecipeProvider`

继承 `RecipeProvider`。用于非 processing 类型的配方 DataGen。

```java
// 注册回调:
register(GeneratedRecipe recipe)
// 资源路径工具:
asResource(String path)
```

`GeneratedRecipe` 函数式接口：`void register(Consumer<FinishedRecipe>)`

### 4.18 `MechanicalCraftingRecipeBuilder` — 机械合成配方构建器

`com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeBuilder`

```java
MechanicalCraftingRecipeBuilder.shapedRecipe(result) / shapedRecipe(result, count)
  .key(Character, TagKey<Item> / ItemLike / Ingredient)
  .patternLine(String)
  .disallowMirrored()
  .whenModLoaded(String) / .whenModMissing(String)
  .withCondition(ICondition)
  .build(Consumer<FinishedRecipe>) / .build(Consumer, ResourceLocation)
```

---

## 5. Equipment API

### 5.1 Goggle 信息接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `IHaveGoggleInformation` | `addToGoggleTooltip(tooltip, isPlayerSneaking)` | 护目镜叠加层显示 |
| `IHaveHoveringInformation` | `addToTooltip(tooltip, isPlayerSneaking)` | 悬浮叠加层显示 |
| `IHaveCustomOverlayIcon` | `getIcon(isPlayerSneaking)` → ItemStack | 自定义叠加图标 |
| `IProxyHoveringInformation` | `getInformationSource(level, pos, state)` → BlockPos | 代理信息源位置 |

`IHaveGoggleInformation` 还包含辅助方法 `containedFluidTooltip(tooltip, playerSneaking, LazyOptional<IFluidHandler>)` 用于显示流体容器信息。

### 5.2 马铃薯火炮

**`PotatoCannonProjectileType`** — 注册 `CreateRegistries.POTATO_PROJECTILE_TYPE`

record 字段：

| 字段 | 类型 | 默认值 |
|------|------|--------|
| `items` | `HolderSet<Item>` | 空 |
| `reloadTicks` | int | 10 |
| `damage` | int | 1 |
| `split` | int | 1 |
| `knockback` | float | 1.0 |
| `drag` | float | 0.99 |
| `velocityMultiplier` | float | 1.0 |
| `gravityMultiplier` | float | 1.0 |
| `soundPitch` | float | 1.0 |
| `sticky` | boolean | false |
| `dropStack` | ItemStack | EMPTY |
| `renderMode` | `PotatoProjectileRenderMode` | Billboard |
| `preEntityHit` | `Optional<PotatoProjectileEntityHitAction>` | empty |
| `onEntityHit` | `Optional<PotatoProjectileEntityHitAction>` | empty |
| `onBlockHit` | `Optional<PotatoProjectileBlockHitAction>` | empty |

Builder 方法：

```java
new PotatoCannonProjectileType.Builder()
    .addItems(Items.POTATO)
    .damage(5).reloadTicks(20).splitInto(3)
    .knockback(1.5f).drag(0.95f).velocity(1.2f).gravity(0.8f)
    .soundPitch(1.5f).sticky()
    .dropStack(new ItemStack(Items.BAKED_POTATO))
    .renderBillboard() / .renderTumbling() / .renderTowardMotion(spriteAngle, spin)
    .preEntityHit(action) / .onEntityHit(action) / .onBlockHit(action)
    .build()
```

**`PotatoProjectileRenderMode`** — 通过 `CreateBuiltInRegistries.POTATO_PROJECTILE_RENDER_MODE` 注册 codec。

**`PotatoProjectileEntityHitAction`** — `PRE_HIT`（返回 true 取消命中）/ `ON_HIT`（返回 true 不回收投射物）。

**`PotatoProjectileBlockHitAction`** — 方块命中处理。

---

## 6. Effect API

### 6.1 `OpenPipeEffectHandler` — 开管流体效果

`com.simibubi.create.api.effect.OpenPipeEffectHandler`

```java
void apply(Level level, AABB area, FluidStack fluid);
// 注册:
OpenPipeEffectHandler.REGISTRY // SimpleRegistry<Fluid, OpenPipeEffectHandler>
```

---

## 7. Event API

### 7.1 `BlockEntityBehaviourEvent<T extends SmartBlockEntity>`

`com.simibubi.create.api.event.BlockEntityBehaviourEvent` (Forge Event)

SmartBlockEntity 反序列化前触发，用于注入/替换 `BlockEntityBehaviour`。

```java
event.attach(BlockEntityBehaviour behaviour);
event.remove(BehaviourType<?> type);
event.getBlockEntity(); // → T
```

### 7.2 `PipeCollisionEvent`

`com.simibubi.create.api.event.PipeCollisionEvent` (Forge Event)

| 子类 | 触发条件 | 特有方法 |
|------|----------|----------|
| `PipeCollisionEvent.Flow` | 管道内两种流体相遇 | `getFirstFluid()`, `getSecondFluid()` |
| `PipeCollisionEvent.Spill` | 管道内流体与世界中流体接触 | `getWorldFluid()`, `getPipeFluid()` |

```java
event.getLevel(); event.getPos();
event.getState(); event.setState(BlockState);
```

### 7.3 `TrackGraphMergeEvent`

`com.simibubi.create.api.event.TrackGraphMergeEvent` (Forge Event)

```java
event.getGraphMergedInto(); event.getGraphMergedFrom();
```

---

## 8. Packager API

### 8.1 `InventoryIdentifier` — 多面存储标识

`com.simibubi.create.api.packager.InventoryIdentifier`

防止多个打包机在同一存储上互相请求物品。

```java
InventoryIdentifier.get(Level, BlockFace face)
InventoryIdentifier.REGISTRY // SimpleRegistry<Block, Finder>
```

`Finder` 接口：`find(Level, BlockState, BlockFace) → InventoryIdentifier`

内置实现：

| record | 说明 |
|--------|------|
| `Single(BlockPos)` | 单个方块 |
| `Pair(BlockPos, BlockPos)` | 两个方块（如双箱子） |
| `Bounds(BoundingBox)` | 包围盒 |
| `MultiFace(BlockPos, Set<Direction>)` | 多个面 |

### 8.2 `UnpackingHandler` — 拆包处理

`com.simibubi.create.api.packager.unpacking.UnpackingHandler` (@Experimental)

| 静态字段 | 说明 |
|----------|------|
| `REGISTRY` | `SimpleRegistry<Block, UnpackingHandler>` |
| `DEFAULT` | `DefaultUnpackingHandler`（默认插入存储） |

```java
boolean unpack(level, pos, state, side, List<ItemStack> items, orderContext, simulate);
```

内置实现：`VoidingUnpackingHandler` — 直接销毁物品。

---

## 9. Schematic API

### 9.1 Safe NBT 写入

| 接口/类 | 方法 | 说明 |
|----------|------|------|
| `PartialSafeNBT` | `writeSafe(CompoundTag)` | BlockEntity 实现此接口 |
| `SafeNbtWriterRegistry.REGISTRY` | `SimpleRegistry<BEType, SafeNbtWriter>` | 注册 SafeNbtWriter |

`SafeNbtWriter`:
```java
void writeSafe(BlockEntity be, CompoundTag tag);
```

### 9.2 Schematic 物品需求

| 接口 | 方法 | 说明 |
|------|------|------|
| `SpecialBlockItemRequirement` | `getRequiredItems(BlockState, BlockEntity)` | 方块需求 |
| `SpecialBlockEntityItemRequirement` | `getRequiredItems(BlockState)` | BE 需求 |
| `SpecialEntityItemRequirement` | `getRequiredItems()` | 实体需求 |

**`SchematicRequirementRegistries`** — 注册表替代方案：

```java
SchematicRequirementRegistries.BLOCKS // SimpleRegistry<Block, BlockRequirement>
SchematicRequirementRegistries.BLOCK_ENTITIES // SimpleRegistry<BEType, BlockEntityRequirement>
SchematicRequirementRegistries.ENTITIES // SimpleRegistry<EntityType, EntityRequirement>
```

### 9.3 Schematic 状态过滤

| 接口/类 | 说明 |
|----------|------|
| `SchematicStateFilter` | Block 实现此接口 |
| `SchematicStateFilterRegistry.REGISTRY` | `SimpleRegistry<Block, StateFilter>` |

`StateFilter`：
```java
BlockState filterStates(@Nullable BlockEntity be, BlockState state);
```

---

## 10. Stress API

### 10.1 `BlockStressValues` — 应力值注册

`com.simibubi.create.api.stress.BlockStressValues`

| 静态字段 | 类型 | 说明 |
|----------|------|------|
| `IMPACTS` | `SimpleRegistry<Block, DoubleSupplier>` | 应力消耗（@1 RPM） |
| `CAPACITIES` | `SimpleRegistry<Block, DoubleSupplier>` | 应力容量（@1 RPM） |
| `RPM` | `SimpleRegistry<Block, GeneratedRpm>` | 转速（tooltip 用） |

```java
double impact = BlockStressValues.getImpact(block);
double capacity = BlockStressValues.getCapacity(block);
BlockStressValues.setGeneratorSpeed(int rpm); // → NonNullConsumer<Block>
BlockStressValues.setGeneratorSpeed(int rpm, boolean mayGenerateLess); // → NonNullConsumer<Block>
```

`GeneratedRpm` record: `(int value, boolean mayGenerateLess)`

---

## 11. Foundation 基类

### 11.1 `ProcessingRecipe<T extends Container>` — 加工配方基类

`com.simibubi.create.content.processing.recipe.ProcessingRecipe`

```java
getIngredients(); getFluidIngredients(); getRollableResults();
getFluidResults(); getProcessingDuration(); getRequiredHeat();
rollResults(); rollResults(List<ProcessingOutput>);
// 子类覆写:
getMaxInputCount(); getMaxOutputCount(); canRequireHeat(); canSpecifyDuration();
getMaxFluidInputCount(); getMaxFluidOutputCount();
readAdditional(json/buffer); writeAdditional(json/buffer);
```

### 11.2 `HeatCondition` 枚举

| 值 | 颜色 | 说明 |
|----|------|------|
| `NONE` | 0xFFFFFF | 不需要热源 |
| `HEATED` | 0xE88300 | 需要加热（Kindled） |
| `SUPERHEATED` | 0x5C93E8 | 需要超热（Seething） |

### 11.3 `IRecipeTypeInfo` — 配方类型信息

```java
ResourceLocation getId();
<T extends RecipeSerializer<?>> T getSerializer();
<T extends RecipeType<?>> T getType();
```

### 11.4 `BlockEntityBehaviour` — BE 行为组件

`com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour`

```java
abstract BehaviourType<?> getType();
initialize(); tick(); lazyTick();
read(CompoundTag, boolean clientPacket); write(CompoundTag, boolean clientPacket);
writeSafe(CompoundTag); isSafeNBT();
getRequiredItems(); onBlockChanged(oldState); onNeighborChanged(neighborPos);
unload(); destroy();
setLazyTickRate(int rate);
static <T> T get(BlockGetter/BlockEntity, BehaviourType<T> type);  // 从 BE 查询 behaviour
```

### 11.5 `MovementContext` — Actor 上下文

`com.simibubi.create.content.contraptions.behaviour.MovementContext`

常用字段：

```java
position, motion, relativeMotion, rotation
world, state, localPos, blockEntityData
stall, disabled, firstMovement, data
contraption, temporaryData
getAnimationSpeed();
getFilterFromBE();
getItemStorage(); // → MountedItemStorage
getFluidStorage(); // → MountedFluidStorage
readNBT(world, info, nbt, contraption)
writeToNBT(CompoundTag)
```

---

## 12. Impl 实现层

### 12.1 `DispenserBehaviorConverter`

`com.simibubi.create.impl.contraption.dispenser.DispenserBehaviorConverter`

将 vanilla `DispenseItemBehavior` 自动包装为 `MountedDispenseBehavior`。
- 投射物行为 → `MountedProjectileDispenseBehavior`
- 其他 → `FallbackBehavior`（出错后永久停用）

黑名单 tag: `AllItemTags.DISPENSE_BEHAVIOR_WRAP_BLACKLIST`

### 12.2 `BlockMovementChecksImpl`

`com.simibubi.create.impl.contraption.BlockMovementChecksImpl`

实现细节参考源码。关键 fallback 逻辑涵盖：
- 活塞推拉反应 (`PushReaction`)
- 动力活塞/轴承/滑轮运行时不可移动
- 轨道/车站不可移动
- 脆弱方块判定（火把、梯子、红石线等）
- 附着关系（梯子、门、床、压力板等）
- 不支撑检测（钻头、锯子、轴承面、活塞伸杆等）

### 12.3 `CreateRegistrateRegistrationCallbackImpl`

注册回调：当 CreateRegistrate 中有条目被注册时通知。
```java
CreateRegistrateRegistrationCallback.register(registryKey, id, callback);
CreateRegistrateRegistrationCallback.provideRegistrate(registrate);
```

---

## 13. Content 层关键基类与接口

### 13.1 `BehaviourType<T>` — 行为类型标识

`com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType`

Identity-based 类型键，用作 `SmartBlockEntity.behaviours` (`Reference2ObjectArrayMap`) 的查询 key。

```java
public class BehaviourType<T extends BlockEntityBehaviour> {
    public BehaviourType();
    public BehaviourType(String name);
    public String getName();
    // hashCode = super.hashCode() * 31 * 493286711
}
```

**设计要点：**
- **不覆写 equals** — 使用引用恒等。每个 `static final` 实例都是唯一的单例。
- `hashCode()` 用 `super.hashCode() * 31 * 493286711` 优化 `Reference2ObjectArrayMap` 哈希分布。
- `name` 可选（默认 `""`），仅用于调试（`getName()`）。

**所有 `static final BehaviourType` 常量（共 26 个）：**

| 常量名 | 所属类 | 用途 |
|--------|--------|------|
| `TYPE` | `AdvancementBehaviour` | 追踪方块放置者，用于成就奖励 |
| `TYPE` | `AnimatedContainerBehaviour<M>` | 统计 GUI 打开人数（方块开启动画） |
| `TYPE` | `DeferralBehaviour` | 调度延迟回调执行 |
| `TYPE` | `EdgeInteractionBehaviour` | 边缘连接交互（如机械臂） |
| `TYPE` | `FilteringBehaviour` | 通用物品/流体过滤；子类 `SidedFilteringBehaviour` 共享、`FactoryPanelBehaviour` 独立 |
| `TYPE` | `ScrollValueBehaviour` | 可滚动的整数值交互；子类 `ScrollOptionBehaviour<E>` 共享 |
| `TYPE`, `INPUT`, `OUTPUT` | `SmartFluidTankBehaviour` | 3 个：主槽、输入槽、输出槽 |
| `TYPE`, `EXTRACT`, `INSERT` | `InvManipulationBehaviour` | 3 个：通用、抽取专用、插入专用 |
| `OBSERVE` | `TankManipulationBehaviour` | 读取流体槽（不写入） |
| `TYPE` | `VersionedInventoryTrackerBehaviour` | 等待库存版本变化（合并操作） |
| `TYPE` | `BracketedBlockEntityBehaviour` | 支架连接（传动轴/管道等） |
| `TYPE` | `DoorControlBehaviour` | 滑动门模式控制 |
| `TYPE` | `BeltProcessingBehaviour` | 传送带/ depot 上物品处理 |
| `TYPE` | `DirectBeltInputBehaviour` | 允许相邻方块向传输带直接输入（Basin/Saw/Depot） |
| `TYPE` | `TransportedItemStackHandlerBehaviour` | 传输带上物品处理回调 |
| `TYPE` | `FluidTransportBehaviour` | 管道流体网络传输与压力 |
| `TYPE` | `FluidDrainingBehaviour` | 软管滑轮抽取逻辑 |
| `TYPE` | `FluidFillingBehaviour` | 软管滑轮填充逻辑 |
| `TYPE` | `LinkBehaviour` | 红石链接收发器 |
| `TYPE` | `DepotBehaviour` | 单个物品持有（Depot/Ejector） |
| `TYPE` | `LogisticallyLinkedBehaviour` | Stock ticker / Packager 网络链接 |
| `TYPE` | `FactoryPanelSupportBehaviour` | Factory gauge → stock ticker 面板链接 |
| `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT` | `FactoryPanelBehaviour` | 4 个：Stock ticker 面板四角过滤槽 |
| `TYPE` | `TrackTargetingBehaviour<T>` | 轨道信号/车站附着 |
| `TYPE` | `AbstractComputerBehaviour` | CC:Tweaked 电脑集成；子类 `ComputerBehaviour` 共享 |

**查询方式（共 71+ 处调用）：**

```java
// BE 内部查询（最常用）：
getBehaviour(SomeBehaviour.TYPE);

// 外部 BE 查询：
BlockEntityBehaviour.get(level, pos, SomeBehaviour.TYPE);      // 静态方法
be.getBehaviour(SomeBehaviour.TYPE);

// Behaviours 注入：
event.attach(behaviour);    // BlockEntityBehaviourEvent 中
attachBehaviourLate(behaviour);  // 运行时注入
```

**典型调用方示例：**

| 调用方 | 行为类型 | 用途 |
|--------|----------|------|
| `SmartBlockEntity.java:260` | `AdvancementBehaviour.TYPE` | 成就奖励 |
| `BasinRecipe.java:142` | `SmartFluidTankBehaviour.INPUT/OUTPUT` | 配方读取流体 |
| `InvManipulationBehaviour.java:101` | `FilteringBehaviour.TYPE` | 过滤检查 |
| `Contraption.java:686` | `FilteringBehaviour.TYPE` | 装置内过滤 |
| `BoilerData.java:348` | `AdvancementBehaviour.TYPE` | 锅炉成就 |
| `BeltFunnelInteractionHandler.java:66` | `InvManipulationBehaviour.TYPE` | 漏斗与传送带交互 |
| `ItemCountDisplaySource.java:23` | `InvManipulationBehaviour.TYPE` | 物品计数显示 |
| `FluidAmountDisplaySource.java:24` | `TankManipulationBehaviour.OBSERVE` | 流体量显示 |
| `AttachedComputerPacket.java:32` | `AbstractComputerBehaviour.TYPE` | CC 集成 |

---

### 13.2 `SmartBlockEntity` — BE 行为组件基类

`com.simibubi.create.foundation.blockEntity.SmartBlockEntity`

所有 Create BlockEntity 的基类。核心数据结构是 `Reference2ObjectArrayMap<BehaviourType<?>, BlockEntityBehaviour>` —— 一个 identity-based、数组后援的 map，针对小数量场景优化。

**继承链：** `BlockEntity` → `SyncedBlockEntity` → `CachedRenderBBBlockEntity` → `SmartBlockEntity`

**`CachedRenderBBBlockEntity` 作用：**

| 方法 | 作用 |
|------|------|
| `getRenderBoundingBox()` | 懒加载缓存 AABB，首次调用后复用 |
| `invalidateRenderBoundingBox()` | 使缓存失效（子类调用, 如 `StationBlockEntity` 进出站时） |
| `createRenderBoundingBox()` | 可覆写，默认 `super.getRenderBoundingBox()` |

---

```java
public abstract class SmartBlockEntity extends CachedRenderBBBlockEntity
    implements PartialSafeNBT, IInteractionChecker, SpecialBlockEntityItemRequirement, VirtualBlockEntity {

    // 构造 → addBehaviours → beatours.put -> execute right away (no NBT available yet)
    public abstract void addBehaviours(List<BlockEntityBehaviour> behaviours);
    // 第一次 NBT 读取时调用 → can access custom NBT
    public void addBehavioursDeferred(List<BlockEntityBehaviour> behaviours);

    // === 生命周期 ===
    public void initialize();       // 首次 tick() 时调用:
                                    //   1. 首次反序列化 → post BlockEntityBehaviourEvent
                                    //   2. forEachBehaviour(BlockEntityBehaviour::initialize)
                                    //   3. lazyTick()
    public void tick();             // 每 tick: initialize首次  → lazyTick 倒计时 → forEachBehaviour(tick)
    public void lazyTick();         // 按 lazyTickRate 调用（默认 10 tick）
    public void invalidate();       // chunk 卸载或销毁时 → forEachBehaviour(unload)
    public void remove();           // 块摧毁或装置拾取时
    public void destroy();          // 块替换时 → forEachBehaviour(destroy)

    // === Behaviour 管理 ===
    public <T extends BlockEntityBehaviour> T getBehaviour(BehaviourType<T> type);
    public void attachBehaviourLate(BlockEntityBehaviour behaviour);
    public void removeBehaviour(BehaviourType<?> type);
    public Collection<BlockEntityBehaviour> getAllBehaviours();

    // === 成就 ===
    public void registerAwardables(List<BlockEntityBehaviour> behaviours, CreateAdvancement... advancements);
    public void award(CreateAdvancement advancement);
    public void awardIfNear(CreateAdvancement advancement, int range);
}
```

**Behaviour 注入时序：**

```
构造:
  addBehaviours(list)       ← 子类注册 behaviours（此时无 NBT 数据）
  list → behaviours map

首次 tick():
  initialize()
    firstNbtRead == true → post BlockEntityBehaviourEvent ← addon 可注入/替换
    forEachBehaviour(initialize)

从 NBT 加载 read():
  firstNbtRead == true:
    addBehavioursDeferred(list)  ← 子类注册依赖 NBT 的 behaviours
    list → behaviours map
    post BlockEntityBehaviourEvent  ← addon 再次注入
  behaviours.read(tag, clientPacket)
```

**所有直接继承 `SmartBlockEntity` 的类（约 77 个）：**

| 类 | 文件 | 代表 behaviour |
|----|------|---------------|
| `KineticBlockEntity` | `.../kinetics/base/KineticBlockEntity.java` | 无（空 `addBehaviours`） |
| `FluidTankBlockEntity` | `.../fluids/tank/FluidTankBlockEntity.java` | 无（ConnectivityHandler 管理） |
| `BasinBlockEntity` | `.../processing/basin/BasinBlockEntity.java` | `DirectBeltInputBehaviour`, `FilteringBehaviour`, `SmartFluidTankBehaviour.INPUT/OUTPUT` |
| `StationBlockEntity` | `.../trains/station/StationBlockEntity.java` | `TrackTargetingBehaviour`, `DoorControlBehaviour`, `DepotBehaviour` |
| `SignalBlockEntity` | `.../trains/signal/SignalBlockEntity.java` | `TrackTargetingBehaviour`, `AbstractComputerBehaviour` |
| `MechanicalPressBlockEntity` | `.../kinetics/press/MechanicalPressBlockEntity.java` | `PressingBehaviour`, 成就 |
| `MechanicalMixerBlockEntity` | `.../kinetics/mixer/MechanicalMixerBlockEntity.java` | `MixingBehaviour`, 成就 |
| `SmartObserverBlockEntity` | `.../redstone/smartObserver/SmartObserverBlockEntity.java` | `FilteringBehaviour`, `VersionedInventoryTrackerBehaviour`, `InvManipulationBehaviour`, `TankManipulationBehaviour` |
| `PackagerBlockEntity` | `.../logistics/packager/PackagerBlockEntity.java` | `InvManipulationBehaviour`, `VersionedInventoryTrackerBehaviour`, 成就 |
| `RedstoneLinkBlockEntity` | `.../redstone/link/RedstoneLinkBlockEntity.java` | `FactoryPanelSupportBehaviour`, `LinkBehaviour`（deferred） |
| `MechanicalArmBlockEntity` | `.../kinetics/mechanicalArm/ArmBlockEntity.java` | `ScrollOptionBehaviour`, 成就 |
| `PortableStorageInterfaceBlockEntity` | `.../contraptions/actors/psi/...` | 仅成就 |
| `ToolboxBlockEntity` | `.../equipment/toolbox/ToolboxBlockEntity.java` | 仅 `AnimatedContainerBehaviour` |
| `ChassisBlockEntity` | `.../contraptions/chassis/ChassisBlockEntity.java` | `ScrollValueBehaviour`（范围） |
| `BrassTunnelBlockEntity` | `.../logistics/tunnel/BrassTunnelBlockEntity.java` | `ScrollOptionBehaviour` + 隧道扩散回调 |
| `ItemVaultBlockEntity` | `.../logistics/vault/ItemVaultBlockEntity.java` | 空（ConnectivityHandler 管理） |
| `BlazeBurnerBlockEntity` | `.../processing/burner/BlazeBurnerBlockEntity.java` | 空 |
| `TrackBlockEntity` | `.../trains/track/TrackBlockEntity.java` | 空 |
| 及其他约 60 个 | （Schematicannon, Depot, Funnel, Chute, SmartChute, 等） | |

---

### 13.3 `KineticBlockEntity` — 动力 BE 基类

`com.simibubi.create.content.kinetics.base.KineticBlockEntity`

继承 `SmartBlockEntity`。所有动力方块的 BE 基类。管理转速、应力、网络传播。

```java
public class KineticBlockEntity extends SmartBlockEntity
    implements IHaveGoggleInformation, IHaveHoveringInformation {

    // === 动力学字段 ===
    public @Nullable Long network;           // KineticNetwork ID
    public @Nullable BlockPos source;        // 旋转来源位置
    protected float speed;                   // 当前转速 (RPM)
    protected float capacity;                // 网络应力容量
    protected float stress;                  // 网络应力负荷
    protected boolean overStressed;          // 是否过载

    // === 转速 ===
    public float getSpeed();                 // overStressed → 0
    public float getTheoreticalSpeed();      // 原始 speed
    public void setSpeed(float speed);
    public float getGeneratedSpeed();        // 默认 0，发生器覆写
    public boolean isSource();               // getGeneratedSpeed() != 0
    public boolean isOverStressed();
    public boolean isSpeedRequirementFulfilled();

    // === 网络 ===
    public boolean hasNetwork();
    public KineticNetwork getOrCreateNetwork();
    public void setNetwork(@Nullable Long network);
    public void attachKinetics();            // → RotationPropagator.handleAdded()
    public void detachKinetics();            // → RotationPropagator.handleRemoved()
    public boolean needsSpeedUpdate();

    // === 应力覆写点 ===
    public float calculateStressApplied();         // 消耗 stress (查 BlockStressValues.IMPACTS)
    public float calculateAddedStressCapacity();   // 提供 stress capacity (查 BlockStressValues.CAPACITIES)
    public void onSpeedChanged(float previousSpeed);

    // === 网络更新 ===
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize);

    // === 源管理 ===
    public boolean hasSource();
    public void setSource(BlockPos source);
    public void removeSource();

    // === 自定义传播（扩展点） ===
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState);
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo,
        BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs);
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours);

    // === 事件 ===
    public void warnOfMovement();
    public void clearKineticInformation();

    // === 工具 ===
    public static float convertToDirection(float axisSpeed, Direction d);
    public static float convertToLinear(float speed);       // speed / 512f
    public static float convertToAngular(float speed);      // speed * 360 / 60 / 20
    public static void switchToBlockState(Level world, BlockPos pos, BlockState state);
}
```

#### `isCustomConnection()` — 详细

默认返回 `false`。覆写后，`RotationPropagator.isConnected()` 将其作为连接条件之一。

**唯一覆写：`GantryShaftBlockEntity`** (`.../kinetics/gantry/GantryShaftBlockEntity.java:73`)

```java
public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
    if (!AllBlocks.GANTRY_CARRIAGE.has(otherState))
        return false;
    final BlockPos diff = other.getBlockPos().subtract(worldPosition);
    Direction direction = Direction.getNearest(diff.getX(), diff.getY(), diff.getZ());
    return otherState.getValue(GantryCarriageBlock.FACING) == direction;
}
```

当相邻方块为 GANTRY_CARRIAGE 且其 FACING 指向本轴时返回 `true`。

**调用处：** `RotationPropagator.isConnected()` (`.../kinetics/RotationPropagator.java:407`)

```java
public static boolean isConnected(KineticBlockEntity from, KineticBlockEntity to) {
    return isLargeCogToSpeedController(...) || getRotationSpeedModifier(from, to) != 0
        || from.isCustomConnection(to, stateFrom, stateTo);
}
```

#### `propagateRotationTo()` — 详细

返回非 0 值表示自定义传播比例；返回 0 回退标准轴/齿轮逻辑。

**调用处唯一：** `RotationPropagator.getRotationSpeedModifier()` (`.../kinetics/RotationPropagator.java:71`)

```java
float custom = from.propagateRotationTo(to, stateFrom, stateTo, diff, connectedByAxis, connectedByGears);
if (custom != 0) return custom;
```

**所有覆写：**

| 类 | 文件:行 | 传播系数 | 条件 |
|----|---------|----------|------|
| `GantryShaftBlockEntity` | `.../gantry/GantryShaftBlockEntity.java:53` | `+1 / -1` (pinion modifier) | 轴通电 + 相邻 GantryCarriage FACING 对准 |
| `GantryCarriageBlockEntity` | `.../gantry/GantryCarriageBlockEntity.java:144` | `+1 / -1` (pinion modifier) | 此 carriage FACING 反向指向通电 GantryShaft |
| `BeltBlockEntity` | `.../belt/BeltBlockEntity.java:546` | `1` | 同一 BeltController 的两个 Belt |
| `ChainConveyorBlockEntity` | `.../chainConveyor/ChainConveyorBlockEntity.java:656` | `1` | 目标在此 chain conveyor 的 connections 列表中 |

#### `addPropagationLocations()` — 详细

默认传入 6 方向邻位列表，子类可追加额外位置。

**调用处唯一：** `RotationPropagator.getPotentialNeighbourLocations()` (`.../kinetics/RotationPropagator.java:439`)

```java
return be.addPropagationLocations(block, blockState, neighbours);
```

**所有覆写：**

| 类 | 文件:行 | 追加位置 |
|----|---------|----------|
| `KineticBlockEntity` | `.../base/KineticBlockEntity.java:554` | 小齿轮：曼哈顿距离 2 的对角线（`(1,1,0)` 等） |
| `SimpleKineticBlockEntity` | `.../simpleRelays/SimpleKineticBlockEntity.java:25` | 大齿轮：所有 `distSqr==2` 位置（12 个方向） |
| `ChainConveyorBlockEntity` | `.../chainConveyor/ChainConveyorBlockEntity.java:650` | 自定义链条连接列表 + 基类对角线 |

#### 所有 `KineticBlockEntity` 子类（约 40 个）

直接继承：

| 类 | 文件 |
|----|------|
| `GeneratingKineticBlockEntity` (abstract) | `.../kinetics/base/GeneratingKineticBlockEntity.java` |
| `LinearActuatorBlockEntity` (abstract) | `.../contraptions/piston/LinearActuatorBlockEntity.java` |
| `GantryCarriageBlockEntity` | `.../contraptions/gantry/GantryCarriageBlockEntity.java` |
| `ClockworkBearingBlockEntity` | `.../contraptions/bearing/ClockworkBearingBlockEntity.java` |
| `MechanicalCrafterBlockEntity` | `.../kinetics/crafter/MechanicalCrafterBlockEntity.java` |
| `TurntableBlockEntity` | `.../kinetics/turntable/TurntableBlockEntity.java` |
| `CuckooClockBlockEntity` | `.../kinetics/clock/CuckooClockBlockEntity.java` |
| `FlapDisplayBlockEntity` | `.../trains/display/FlapDisplayBlockEntity.java` |
| `ChainGearshiftBlockEntity` | `.../kinetics/chainDrive/ChainGearshiftBlockEntity.java` |
| `ChainConveyorBlockEntity` | `.../kinetics/chainConveyor/ChainConveyorBlockEntity.java` |
| `SpeedControllerBlockEntity` | `.../kinetics/speedController/SpeedControllerBlockEntity.java` |
| `SimpleKineticBlockEntity` | `.../kinetics/simpleRelays/SimpleKineticBlockEntity.java` |
| `BeltBlockEntity` | `.../kinetics/belt/BeltBlockEntity.java` |
| `DirectionalShaftHalvesBlockEntity` | `.../kinetics/base/DirectionalShaftHalvesBlockEntity.java` |
| `GaugeBlockEntity` (abstract) | `.../kinetics/gauge/GaugeBlockEntity.java` |
| `BlockBreakingKineticBlockEntity` (abstract) | `.../kinetics/base/BlockBreakingKineticBlockEntity.java` |
| `GantryShaftBlockEntity` | `.../kinetics/gantry/GantryShaftBlockEntity.java` |
| `EncasedFanBlockEntity` | `.../kinetics/fan/EncasedFanBlockEntity.java` |
| `FlywheelBlockEntity` | `.../kinetics/flywheel/FlywheelBlockEntity.java` |
| `MillstoneBlockEntity` | `.../kinetics/millstone/MillstoneBlockEntity.java` |
| `FluidValveBlockEntity` | `.../fluids/pipes/valve/FluidValveBlockEntity.java` |
| `HosePulleyBlockEntity` | `.../fluids/hosePulley/HosePulleyBlockEntity.java` |
| `PumpBlockEntity` | `.../fluids/pump/PumpBlockEntity.java` |
| `DeployerBlockEntity` | `.../kinetics/deployer/DeployerBlockEntity.java` |
| `ArmBlockEntity` | `.../kinetics/mechanicalArm/ArmBlockEntity.java` |
| `CrushingWheelBlockEntity` | `.../kinetics/crusher/CrushingWheelBlockEntity.java` |
| `BacktankBlockEntity` | `.../equipment/armor/BacktankBlockEntity.java` |
| `BasinOperatingBlockEntity` (abstract) | `.../processing/basin/BasinOperatingBlockEntity.java` |
| `EjectorBlockEntity` | `.../logistics/depot/EjectorBlockEntity.java` |

间接继承层次：

```
KineticBlockEntity
  ├─ GeneratingKineticBlockEntity         → WaterWheelBE, PoweredShaftBE, MechanicalBearingBE, CreativeMotorBE, HandCrankBE
  ├─ LinearActuatorBlockEntity            → MechanicalPistonBE, PulleyBE
  ├─ DirectionalShaftHalvesBlockEntity    → GearboxBE, SplitShaftBE → SequencedGearshiftBE, GearshiftBE, ClutchBE
  ├─ GaugeBlockEntity                     → StressGaugeBE, SpeedGaugeBE
  ├─ BlockBreakingKineticBlockEntity      → SawBE, DrillBE
  ├─ SimpleKineticBlockEntity             → BracketedKineticBE
  └─ BasinOperatingBlockEntity            → MechanicalPressBE, MechanicalMixerBE
```

---

### 13.4 `GeneratingKineticBlockEntity` — 发生器 BE 基类

`com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity`

继承 `KineticBlockEntity`。所有主动产生动力的 BE 基类（水轮、引擎、手摇曲柄等）。

**所有子类（5 个）：**

| 类 | 文件 | 备注 |
|----|------|------|
| `WaterWheelBlockEntity` | `.../kinetics/waterwheel/WaterWheelBlockEntity.java` | 水轮/大水轮，基于水流计算转速 |
| `PoweredShaftBlockEntity` | `.../kinetics/steamEngine/PoweredShaftBlockEntity.java` | 蒸汽引擎动力输出轴 |
| `MechanicalBearingBlockEntity` | `.../contraptions/bearing/MechanicalBearingBlockEntity.java` | 机械轴承旋转结构 |
| `CreativeMotorBlockEntity` | `.../kinetics/motor/CreativeMotorBlockEntity.java` | 创造马达，固定转速 |
| `HandCrankBlockEntity` | `.../kinetics/crank/HandCrankBlockEntity.java` | 手摇曲柄，玩家操作产生短时转速 |

**核心方法：**

```java
public void updateGeneratedRotation() {
    float speed = getGeneratedSpeed();         // 子类提供
    float prevSpeed = this.speed;
    // ... 速度变化检查，通知网络
    applyNewSpeed(prevSpeed, speed);
    // 同步网络应力
    notifyStressCapacityChange(calculateAddedStressCapacity());
    getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
    onSpeedChanged(prevSpeed);
    sendData();
}
```

`applyNewSpeed()` 决定行为：
- `speed=0` → `detachKinetics()` + `setNetwork(null)`
- `prevSpeed=0` → 创建新网络 + `attachKinetics()`
- 有 source 且被超越 → `detachKinetics()` + 自建网络成为新 source
- 默认 → 更新 speed

---

### 13.5 `KineticBlock` — 动力方块基类

`com.simibubi.create.content.kinetics.base.KineticBlock`

所有动力方块的 Block 基类。唯一直接 `implements IRotate` 的类。

```java
public abstract class KineticBlock extends Block implements IRotate {
    protected boolean areStatesKineticallyEquivalent(BlockState old, BlockState newState);
    public float getParticleTargetRadius();    // 默认 0.65
    public float getParticleInitialRadius();   // 默认 0.75
}
```

**关键行为：**
- `onPlace()` 检查新旧状态 `areStatesKineticallyEquivalent()` → 是则设置 `preventSpeedUpdate = 2`，避免不必要的网络重算。
- `updateIndirectNeighbourShapes()` 触发 `clearKineticInformation()` + 标记 `updateSpeed = true`。
- `setPlacedBy()` → 成就追踪 + `queueRotationIndicators()`。

**`areStatesKineticallyEquivalent()` 判断：** `oldState.getBlock() == newState.getBlock() && getRotationAxis(newState) == getRotationAxis(oldState)`。

**所有 `extends KineticBlock` 的层级（约 34+ 类）：**

```
KineticBlock (abstract)
  ├─ RotatedPillarKineticBlock (abstract)         ← AXIS property
  │    ├─ AbstractShaftBlock (abstract)
  │    │    ├─ AbstractSimpleShaftBlock → ShaftBlock, CogWheelBlock
  │    │    └─ PoweredShaftBlock
  │    ├─ AbstractEncasedShaftBlock → EncasedShaftBlock, GearshiftBlock
  │    ├─ ChainDriveBlock
  │    ├─ CrushingWheelBlock
  │    ├─ FlywheelBlock
  │    ├─ GearboxBlock
  │    ├─ LargeWaterWheelBlock
  │    └─ EncasedCogwheelBlock
  │
  ├─ HorizontalKineticBlock (abstract)            ← HORIZONTAL_FACING property
  │    ├─ BeltBlock, CuckooClockBlock, BacktankBlock
  │    ├─ MechanicalPressBlock, MechanicalCrafterBlock
  │    ├─ FlapDisplayBlock, HosePulleyBlock
  │    ├─ EjectorBlock, ElevatorPulleyBlock
  │
  ├─ HorizontalAxisKineticBlock (abstract)         ← HORIZONTAL_AXIS property
  │    ├─ SequencedGearshiftBlock, SpeedControllerBlock
  │    ├─ PulleyBlock, GirderEncasedShaftBlock
  │
  ├─ DirectionalKineticBlock (abstract)            ← FACING property
  │    ├─ BearingBlock (abstract)
  │    │    ├─ WindmillBearingBlock, MechanicalBearingBlock
  │    │    └─ ClockworkBearingBlock
  │    ├─ DirectionalAxisKineticBlock (abstract)   ← + AXIS_ALONG_FIRST_COORDINATE
  │    │    ├─ SawBlock, DeployerBlock
  │    │    ├─ GaugeBlock (abstract)
  │    │    ├─ FluidValveBlock, GantryCarriageBlock
  │    │    ├─ MechanicalPistonBlock
  │    ├─ DrillBlock, EncasedFanBlock
  │    ├─ HandCrankBlock, CreativeMotorBlock
  │    ├─ PumpBlock, WaterWheelBlock
  │    ├─ GantryShaftBlock
  │
  ├─ TurntableBlock, MillstoneBlock
  ├─ MechanicalMixerBlock
  ├─ ArmBlock, ChainConveyorBlock
```

---

### 13.6 `IRotate` — 旋转方块接口

`com.simibubi.create.content.kinetics.base.IRotate`

所有旋转方块必须实现的接口。继承 `IWrenchable`（扳手交互）。

```java
public interface IRotate extends IWrenchable {
    // 此面是否有轴（用于连接传播）：
    boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face);
    // 旋转轴：
    Axis getRotationAxis(BlockState state);
    // 最低转速要求（护目镜提示）：
    default SpeedLevel getMinimumRequiredSpeedLevel() { return SpeedLevel.NONE; }
    // 是否在护目镜界面隐藏应力消耗显示：
    default boolean hideStressImpact() { return false; }
    // 是否显示 "generate" 前缀（生成器标注）：
    default boolean showCapacityWithAnnotation() { return false; }
}
```

**内部枚举：**

```java
enum SpeedLevel {
    NONE (0 RPM,  Dark Gray),
    SLOW (1 RPM,  Green),
    MEDIUM(20 RPM, Aqua),
    FAST (30 RPM, Light Purple);
    static SpeedLevel of(float speed);         // 根据速度返回等级
    float getSpeedValue();                     // 速度阈值
    static LangBuilder getFormattedSpeedText(float speed, boolean overstressed);
}

enum StressImpact {
    LOW (<50%), MEDIUM (50-75%), HIGH (75-100%), OVERSTRESSED (>100%);
    static StressImpact of(double stressPercent);
    static boolean isEnabled();                // disableStress 配置
    static LangBuilder getFormattedStressText(double stressPercent);
}
```

**`hasShaftTowards` 实现模式（30+ 类）：**

| 模式 | 代表类 | 逻辑 |
|------|--------|------|
| `return face.getAxis() == state.getValue(AXIS)` | `AbstractShaftBlock`, `AbstractEncasedShaftBlock`, `ChainDriveBlock`, `CrushingWheelBlock`, `FlywheelBlock`, `GearboxBlock`, `LargeWaterWheelBlock` | 基于 `AXIS` property |
| `return face.getAxis() == state.getValue(HORIZONTAL_AXIS)` | `HorizontalAxisKineticBlock`, `SequencedGearshiftBlock`, `SpeedControllerBlock`, `PulleyBlock` | 基于 `HORIZONTAL_AXIS` property |
| `return face.getAxis() == getRotationAxis(state)` | `DirectionalAxisKineticBlock`, `SawBlock`, `DeployerBlock`, `GaugeBlock`, `FluidValveBlock`, `GantryCarriageBlock`, `MechanicalPistonBlock`, `DrillBlock`, `EncasedFanBlock`, `HandCrankBlock`, `CreativeMotorBlock`, `PumpBlock`, `WaterWheelBlock`, `GantryShaftBlock`, `BearingBlock` | 转为 axis |
| `return face.getAxis() == state.getValue(HORIZONTAL_FACING).getClockWise().getAxis()` | `BeltBlock`, `EjectorBlock` | 水平 facing 的垂直轴 |
| `return true` for specific face | `TurntableBlock` (UP), `MillstoneBlock` (DOWN), `MechanicalMixerBlock` (DOWN), `DrillBlock` (facing opposite) | 固定面 |
| `return false` | `KineticBlock` 基类默认 | 无轴连接 |

**`getMinimumRequiredSpeedLevel` 覆写：**

| 类 | 返回 | 含义 |
|----|------|------|
| 默认 | `NONE` | 无要求 |
| `MechanicalMixerBlock` | `MEDIUM` (20 RPM) | 搅拌器需中速 |
| `FlapDisplayBlock` | `FAST` (30 RPM) | 翻牌显示需高速 |

**`hideStressImpact` 覆写：** `WaterWheelBlock`, `CreativeMotorBlock` — 返回 `true`（不显示应力消耗，因为是纯发生器）

**`showCapacityWithAnnotation` 覆写：** `BearingBlock` — 返回 `true`（显示 "generate" 前缀标注）

---

### 13.7 `ProcessingOutput` — 带几率配方输出

`com.simibubi.create.content.processing.recipe.ProcessingOutput`

```java
public class ProcessingOutput {
    public static final ProcessingOutput EMPTY;         // ItemStack.EMPTY, chance=1

    public ProcessingOutput(ItemStack stack, float chance);
    // Datagen 兼容：
    public ProcessingOutput(Pair<ResourceLocation, Integer> item, float chance);

    public ItemStack getStack();
    public float getChance();                           // 0~1
    public ItemStack rollOutput();                      // 按 chance 独立 roll

    // 序列化：
    public JsonElement serialize();
    public static ProcessingOutput deserialize(JsonElement je);
    public void write(FriendlyByteBuf buf);
    public static ProcessingOutput read(FriendlyByteBuf buf);
}
```

**`rollOutput()` 算法：**

```java
for (int roll = 0; roll < stack.getCount(); roll++)
    if (r.nextFloat() > chance) outputAmount--;
```

每个物品独立判断。例：`stack=钻石x3, chance=0.5` → 最终产出可能是 `0/1/2/3` 个钻石，期望值 `1.5`。

**使用场景：** 所有 `ProcessingRecipe` 子类的 `getRollableResults()` 返回 `List<ProcessingOutput>`，在配方完成时调用 `rollResults()` 遍历并 roll。

---

### 13.8 `FluidTankBlockEntity` — 流体罐（多块结构示例）

`com.simibubi.create.content.fluids.tank.FluidTankBlockEntity`

继承 `SmartBlockEntity`，实现 `IMultiBlockEntityContainer.Fluid`。展示如何使用 `ConnectivityHandler` 管理多块结构。

**`IMultiBlockEntityContainer` 接口层次：**

```java
public interface IMultiBlockEntityContainer {
    BlockPos getController();  BlockEntity getControllerBE();
    boolean isController();    void setController(BlockPos pos);
    void removeController(boolean keepContents);
    int getWidth();   void setWidth(int width);
    int getHeight();  void setHeight(int height);
    int getMaxWidth();
    int getMaxLength(Direction.Axis axis, int width);
    Direction.Axis getMainConnectionAxis();
    Object getExtraData();  void setExtraData(Object data);
    void preventConnectivityUpdate();  void notifyMultiUpdated();

    interface Inventory extends IMultiBlockEntityContainer {
        default boolean hasInventory() { return false; }
    }
    interface Fluid extends IMultiBlockEntityContainer {
        default boolean hasTank() { return false; }
        IFluidTank getTank(int index);
        void setTankSize(int index, int blocks);
        FluidStack getFluid(int index);
        int getTankSize(int index);
    }
}
```

**仅有的 2 个实现：**

| 类 | 接口 | 轴 | 存储 | 最大尺寸 |
|----|------|-----|------|----------|
| `FluidTankBlockEntity` | `.Fluid` | Y (垂直) | `SmartFluidTank` | 3×3×任意 |
| `ItemVaultBlockEntity` | `.Inventory` | X/Z (水平) | `ItemStackHandler` | 3×3×配置 |

**`FluidTankBlockEntity` 核心：**

```java
public class FluidTankBlockEntity extends SmartBlockEntity
    implements IHaveGoggleInformation, IMultiBlockEntityContainer.Fluid {

    protected FluidTank tankInventory;          // 实际存储
    protected BlockPos controller;              // 控制器位置
    protected int width;                        // 宽度/深度（max 3）
    protected int height;                       // 高度
    public BoilerData boiler;                   // 锅炉数据

    // 自动连接：
    public void updateConnectivity() {
        ConnectivityHandler.formMulti(this);
    }
}
```

#### `ConnectivityHandler` 多块验证流程

`formMulti(be)` → `tryToFormNewMultiOfWidth(be, axis, origin, width)`:

1. **扫描候选体积：** 沿连接轴逐层遍历
2. **类型匹配：** 每个位置必须存在匹配的 `BlockEntityType`
3. **宽度判断：** 现有控制器宽度 ≤ 候选宽度才可合并
4. **满容判断：** 同宽度满高度控制器不可再合并
5. **轴一致性：** 所有部件连接轴必须相同
6. **边界检查：** 垂直（Y 轴）与水平（X/Z 轴）边界规则不同
7. **流体兼容：** 不同流体类型不可合并
8. **实际形成：** 设置控制器、合并流体、`splitMultiAndInvalidate` 使各部件失效

**`splitMultiAndInvalidate` 分割流程：**
1. 1×1 提前返回
2. 流体分布到分割后各部件
3. `partAt.removeController(true)` 移除所有连接
4. capability 组合失效
5. 可选 `tryReconnect` 尝试形成更小子组

#### 锅炉联动

`FluidTankBlockEntity.boiler` 是 `BoilerData` 实例，管理：

| 字段/方法 | 作用 |
|-----------|------|
| `attachedEngines` | 连接蒸汽引擎数量 |
| `attachedWhistles` | 笛子数量 |
| `activeHeat` | 主动热源总值（`BoilerHeater.findHeat`） |
| `passiveHeat` | 是否有被动热源 |
| `waterSupply` | 水量供应速率 |
| `tick(FluidTankBlockEntity controller)` | 每 tick 测量水量、音频/粒子 |
| `updateTemperature(FluidTankBlockEntity controller)` | 扫描控制器下方方块的热量 |
| `evaluate(FluidTankBlockEntity controller)` | 遍历多块每个面，检测引擎/笛子数量 |
| `getEngineEfficiency(int boilerSize)` | 计算引擎效率（受热量/水/尺寸限制） |

**蒸汽引擎连接流程：**

```
SteamEngineBlock.onPlace() / onRemove()
  → FluidTankBlock.updateBoilerState()
    → FluidTankBlockEntity controller.updateBoilerState()
      → boiler.evaluate(controller)      // 扫描引擎/笛子
      → boiler.updateTemperature(controller)  // 扫描热量

SteamEngineBlockEntity.tick()
  → tank = getTank()                     // WeakReference<FluidTankBlockEntity>
  → tank.boiler.getEngineEfficiency(tank.getTotalTankSize())
  → 效率 0~1，影响动力输出
```

**引用 `FluidTankBlockEntity` 的文件：**

| 文件 | 引用原因 |
|------|----------|
| `SteamEngineBlockEntity.java` | `WeakReference<FluidTankBlockEntity>` 作为引擎附着对象 |
| `SteamEngineBlock.java` | `canAttach` 检查是否为 `FluidTankBlock` |
| `WhistleBlockEntity.java` | `WeakReference<FluidTankBlockEntity>` 获取锅炉数据 |
| `BoilerDisplaySource.java` | 展板显示锅炉状态 |
| `FluidTankItem.java` | 放置时检查容量、调用 `ConnectivityHandler.partAt` |
| `FluidTankCTBehaviour.java` | 连接纹理（`ConnectivityHandler.isConnected`） |
| `FluidTankModel.java` | 模型面剔除（`ConnectivityHandler.isConnected`） |
| `FluidTankMovementBehavior.java` | 装置移动时流体罐行为 |
| `FluidTankMountedStorage.java` | 挂载存储实现 |
| `CreativeFluidTankBlockEntity.java` | 继承 `FluidTankBlockEntity` |

---

## 附录：Create Registers for Registrate helpers

| API 静态方法 | 注册到的 SimpleRegistry |
|-------------|------------------------|
| `DisplaySource.displaySource(src)` | `DisplaySource.BY_BLOCK` |
| `DisplayTarget.displayTarget(tgt)` | `DisplayTarget.BY_BLOCK` |
| `MovingInteractionBehaviour.interactionBehaviour(bhv)` | `MovingInteractionBehaviour.REGISTRY` |
| `MovementBehaviour.movementBehaviour(bhv)` | `MovementBehaviour.REGISTRY` |
| `MountedItemStorageType.mountedItemStorage(type)` | `MountedItemStorageType.REGISTRY` |
| `MountedFluidStorageType.mountedFluidStorage(type)` | `MountedFluidStorageType.REGISTRY` |
| `BlockStressValues.setGeneratorSpeed(rpm)` | `BlockStressValues.RPM` |

所有方法均返回 `NonNullConsumer<Block>` / `NonNullUnaryOperator<BlockBuilder>`，
可在 Registrate builder 链中直接使用。
