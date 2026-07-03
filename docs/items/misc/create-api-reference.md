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
| `onRegister(Runnable invalidate)` | 注册后的回调 |
| `forTag(TagKey<K>, Function<K,Holder<K>>, V)` | 为整个 tag 提供值（自动 tag reload 时 invalidate） |
| `forBlockTag(...)`/`forItemTag(...)`/`forEntityTag(...)`/`forFluidTag(...)` | 各类型的快捷方法 |

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

`com.simibubi.create.api.behaviour.spouting.BlockingSpoutingBehaviour`

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
