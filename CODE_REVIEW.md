# 全员代码审查报告（修正版）

> 审查日期: 2026-06-27 | 修正版
> 审查范围: 全部 161 个 Java 源文件
> 审查方法: 多路并行审查 + 文档交叉验证
> 文档参考: `docs/items/`, `docs/incidents/`, `PROTOTYPE_ARCHITECTURE.md`, `openspec/`

---

## 变更记录

| 原报告问题 | 修正结果 | 依据 |
|-----------|---------|------|
| `SquadBarracksScreen` 物品不同步 (C3) | ❌ **误报，撤销** | 配置槽是虚拟槽，不消耗物品是设计意图 |
| `RemoteEngineBlockItem.getBoundPos` NPE | ❌ **误报，撤销** | `isBound()` 已守卫 null tag |
| `SquadFocus.offhand.get(0)` 越界 | ❌ **误报，撤销** | `NonNullList` 固定 size 1 |
| `PrototypeItem.hasFlight` 未用参数 | ❌ **误报，撤销** | 设计决策：滑翔改为始终可用 (`prototype.md:102`) |
| `MinionSpawnHelper` 折扣不生效 | ⚠️ **降级** | `effectiveCost` 仅用做门槛判断；实际扣费由 Goety `decreaseSouls` 内部处理（需读 Goety 源码确认） |
| `TimeStopUtils` 计数器清零目标反转 | ⚠️ **降级** | 逻辑可疑但可能有意（拒绝他人解冻），需读 Goety EndLibrary 源码确认 |
| `TimeStopRandom` 字段遮蔽 | ❌ **误报，撤销** | `AtomicLong seed` 遮蔽父类字段 — 但该类独立于父类的随机状态，不会导致问题 |
| `HumanoidArmorLayerMixin` 包路径错 | ❌ **误报，撤销** | 文件实际在 `com.goetypunk.addon.mixin` 包，与 mixins.json 配置一致 |
| `HumanoidArmorLayerMixin` 附魔参数错 | ❌ **误报，撤销** | 二次渲染已用 `if (hasFoil)` 守卫，glint/false 基础层+true 闪光层是标准模式 |
| `CrazedIllagerEntity` 自伤 (G1) | ❌ **误报，撤销** | `crazed-illager.md:7` 记录为设计决策 |
| 所有 [TEST]/[WheelTest] 日志 | ✅ **确认** | 残留调式代码，docs 未注明其必要性 |

---

## 结论汇总

| 等级 | 修正前 | 修正后 | 净变化 |
|------|--------|--------|--------|
| 🔴 崩溃级 | 4 | **2** | -2 |
| 🔴 玩法级 | 3 | **1**（均为待定） | -2 |
| 🟡 日志污染 | ~40 处 | **~40 处** | 不变 |
| 🟡 逻辑缺陷 | 8 | **5** | -3 |
| 🟡 性能 | 5 | **5** | 不变 |
| 🔵 代码异味 | ~12 | **~10** | -2 |

---

# 🔴 崩溃级

## C1. `FleshFurnaceBlockEntity.getCapability()` — 数组索引错乱 + EAST 面崩溃

- **文件**: `src/main/java/com/goetypunk/addon/common/block/entity/FleshFurnaceBlockEntity.java:70, 584`
- **问题**: `SidedInvWrapper.create(this, Direction.UP, NORTH, SOUTH, EAST, WEST)` 创建 5 元素数组（索引 0-4）。但 `handlers[side.get3DDataValue()]` 的索引对应标准方向序（DOWN=0, UP=1, NORTH=2, SOUTH=3, WEST=4, EAST=5）。
  - UP(→1) → handlers[1] → **取到 NORTH handler**
  - NORTH(→2) → handlers[2] → **取到 SOUTH handler**
  - SOUTH(→3) → handlers[3] → **取到 EAST handler**
  - EAST(→5) → handlers[5] → **`ArrayIndexOutOfBoundsException`** 🚨
  - WEST(→4) → handlers[4] → 正好取到 WEST handler（巧合正确）
- **影响**: 对熔炉 EAST 面的漏斗/管道操作直接崩溃。所有其他方向（除 WEST）映射到错误 handler。
- **修复**: `SidedInvWrapper.create(this, Direction.values())` 或者：
  ```java
  private final LazyOptional<IItemHandlerModifiable>[] handlers = SidedInvWrapper.create(this,
      Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
  ```

## C2. `TimeContext.safeClientFrameTime()` 缺少 `@OnlyIn`

- **文件**: `src/main/java/com/goetypunk/addon/common/time/TimeContext.java:15-17`
- **问题**: 调用 `Minecraft.getInstance().getFrameTime()` 但无 `@OnlyIn(Dist.CLIENT)`。在 Dedicated Server 上抛出 `ClassNotFoundException` 或直接崩溃。
- **修复**: 加 `@OnlyIn(Dist.CLIENT)`。

---

# 🔴 玩法级

## ~~G1. `CrazedIllagerEntity` / `CrazedIllagerServant` — 自伤导致 3 秒自杀~~ → ❌ **误报，撤销**

- **重新评估**: `docs/items/misc/crazed-illager.md:7` 明确记录 "攻击机制改为 4 帧/次 × 5 次/秒，无视无敌帧，**自伤 1 HP**" 为 2026-06-25 的故意改动。
- **结论**: 自伤是设计决策，非 bug。虽然实战中 15 HP / 1/tick = 3 秒自杀看似极端，但文档记录为有意行为，不予标记。
- **保留意见**: 建议开发者在文档中补充自伤的玩法意图（如狂暴/怒气机制），以便未来回顾。

## G2. ~~`MinionSpawnHelper` 灵魂折扣不生效~~ → **待定，需验证 Goety 源码**

- **文件**: `src/main/java/com/goetypunk/addon/common/utils/MinionSpawnHelper.java:163-181`
- **问题表面**: `effectiveCost = Math.max(1, (int)(soulCost * discount))` 计算了折扣价但扣费仍用 `soulCost`。
- **证据反转**: 方法注释明确写"扣 soulCost（内部打折）"，暗示折扣由 Goety 的 `SEHelper.decreaseSouls()` 内部处理。
- **结论**: **不能确认是 bug**。如果 `SEHelper.decreaseSouls()` 内部自动应用 `soulDiscount`，则逻辑正确。需读 `D:\Libcode\Goety-2-1.20\` 中 `SEHelper.java` 源码确认。
- **修复**: 如果 Goety 不处理内部折扣，应将 `decreaseSouls(player, soulCost)` 改为 `decreaseSouls(player, effectiveCost)`。

## G3. ~~`TimeStopUtils.use() !z 分支计数器清零目标反转`~~ → **待定，需验证设计意图**

- **文件**: `src/main/java/com/goetypunk/addon/common/time/TimeStopUtils.java:59`
- **问题表面**: `setTimeStopCount(source, 0)` 设置来源者（而不是其他实体）的计时器为 0。
- **双重解释**:
  - **Bug 解释**: 应改为 `setTimeStopCount(living, 0)` — 清除其他实体的计时器以允许全局解冻
  - **Design 解释**: "其他人还在冻结 → 拒绝你的解冻请求，把你的计数器清空，让你重新冻结时从 0 开始计时"
- **文档对照**: `docs/time-focus-pouch-architecture.md` 记录时停系统源自 EndLibrary，但未描述此处的逻辑细节。
- **结论**: 语义不明确，需读 EndLibrary 原始实现对照。

---

# 🟡 日志污染

> 以下文件包含残留的 `LOGGER.info()` / `LOGGER.warn()` / `LOGGER.error()` 调式输出。  
> 项目已进入生产阶段，这些日志应降级为 `LOGGER.debug()` 或直接移除。

## L1. `PrototypeEvents.java` — ~30 处 INFO 日志

| 行号 | 前缀 | 说明 |
|------|------|------|
| 261 | `D START` | 每 tick |
| 281 | `PROTO_DIAG START` | 每 tick |
| 287, 292 | `Prototype ground takeoff/mid-air` | 触发时 |
| 303 | `D POST_TRAVEL` | 每 tick |
| 331, 342 | `SprintTick / SprintMod AFTER` | 每 tick |
| 369, 377, 389 | `PROTO_DIAG CJ_CLEAR/INJECT` / `D END` | 蓄力时每 tick |
| 427, 429, 455 | `Prototype wall-run LIFT/START/CORNER` | 墙跑时 |
| 562, 582, 593 | `Sprint inertia / PROTO_DIAG` | 蓄力时 |
| 694, 735-763 | `SlamImpact / AoE` | Slam 时 |
| 881, 944-976 | `AoE particle / ring[]` | Slam 时 |

## L2. `PrototypePlayerMixin.java` — 4 处每 tick INFO

- 行 23-24: `MIXIN_AISTEP_HEAD` — 每个玩家每 tick
- 行 29-30: `MIXIN_AISTEP_RETURN` — 每个玩家每 tick
- 行 37-38, 43-44: `Prototype Mixin: cancelled jump`

## L3. `PrototypeRenderEvents.java` — 每帧日志

- 行 213-218: `CLIENT_CAMERA: rollDeg=...`
- 行 335-337: `CLIENT_WALL: sprint=...`

## L4. `RemoteEngineBlockEntity.java` — 7 处 `[TEST]` 前缀

- 行 56-169: `LoggerFactory.getLogger("TEST")`，输出 `[TEST] RE{} INIT/REFRESH/RPM/OSCILLATION/STRESS/LINK/GEN`
- **额外问题**: Logger 名称 `"TEST"` — 如果 logback.xml 按 Logger 名过滤，这些日志可能无法被统一控制。

## L5. `StressReaderBlockEntity.java` — 3 处 `[TEST]` 前缀

- 行 75-110: 同样 `LoggerFactory.getLogger("TEST")`

## L6. `CrushingWheelEntity.java` — ~10 处 `[WheelTest]` 前缀

- 行 55-163: `LOGGER.info("[WheelTest] ...")` + `LOGGER.debug(...)`。每 tick 输出。

## L7. `CrushingWheelSpell.java` — 1 处

- 行 131-132: `LOGGER.info("StressFocus spell → ...")`

## L8. `ModClientEvents.java` — 级别错误

- 行 43: `LOGGER.error("[MOD_CLIENT] onClientSetup called")` — 正常初始化应使用 info
- 行 61: `LOGGER.error("[MOD_CLIENT] Flywheel Visuals DISABLED")` — 正常路径应使用 info

## L9. `ModClientForgeEvents.java` — 3 处

- 行 58: `LOGGER.info("X released...")`
- 行 79: `LOGGER.info("freezePacketDelay reached 0...")`
- 行 133: `LOGGER.info("onScreenClosed for {}")`

---

# 🟡 逻辑缺陷

## B1. `CrazedIllagerEntity` — AI 目标优先级冲突

- **文件**: `CrazedIllagerEntity.java:77-78`
- **问题**: 两个 `NearestAttackableTargetGoal` 都设为优先级 `3` → 非确定性行为
- **修复**: 改为 `3` 和 `4`。

## B2. `CrazedSummonSpell` / `SquadSpell` — 全量实体遍历

- **文件**: `CrazedSummonSpell.java:76`, `SquadSpell.java:91`
- **问题**: `worldIn.getAllEntities()` 遍历世界所有实体。应当在 `AABB` 内用 `getEntitiesOfClass()` 过滤。
- **影响**: 大型世界（多玩家/多实体）性能开销显著。

## B3. `TimeStopUtils.java` — 全量 LivingEntity 遍历

- **文件**: `TimeStopUtils.java:55-56`
- **问题**: `AABB(0,0,0).inflate(30000000)` 遍历所有加载活体实体。每次时停操作触发。
- **建议**: 改用跟踪列表（`TimeStopSavedData` 扩展计数器）替代全量扫描。

## B4. `FleshCasketItem.getShareTag()` — 修改源 NBT

- **文件**: `FleshCasketItem.java:99`
- **问题**: `getShareTag()` 将 `cap` 键写入物品栈 NBT。getter 不应有副作用。
- **修复**: `stack.getTag().copy()` 创建副本操作。

## B5. `FleshCasketItemCapability.deserializeNBT()` — 无类型检查

- **文件**: `FleshCasketItemCapability.java:48`
- **问题**: `(CompoundTag) nbt` 无 `instanceof` 检查。
- **修复**: `if (nbt instanceof CompoundTag tag) { ... }`。

## B6. `FleshFurnaceBlockEntity.java:84` — `Math.abs(Integer.MIN_VALUE)` 溢出

- **文件**: `FleshFurnaceBlockEntity.java:84`
- **问题**: `Math.abs(Integer.MIN_VALUE)` 返回负数。极罕见情况下熔炉扫描永不触发。
- **修复**: `Math.floorMod(worldPosition.hashCode(), 60)`。

## B7. `RiftWalkEvents.java` — 缺少空字符串守卫

- **文件**: `RiftWalkEvents.java:52`
- **问题**: `data.getString(TAG_ORIGIN_DIM)` 返回 `""` 时 `new ResourceLocation("")` 抛出异常。
- **修复**: 检查 `isEmpty()`。

## B8. `C2STimeStopPacket.java` — 硬编码 180 tick

- **文件**: `C2STimeStopPacket.java:28`
- **问题**: 硬编码 `180` 而非 `ModConfig.TIME_STOP_DEFAULT_DURATION.get()`
- **修复**: 使用配置值。

---

# 🟡 性能问题

## P1. `PrototypeTravelMixin` — 三 injector 重复守卫检查

- **文件**: `PrototypeTravelMixin.java:22, 70, 131`
- **问题**: 三个 `@Inject` 各自独立调 `PrototypeItem.hasAnyPrototype(player)`
- **建议**: 合并为单个前置 injector 缓存结果。

## P2. `SquadFocus.findAllBoundFoci()` — 全量背包遍历

- **文件**: `SquadFocus.java:120-130`
- **问题**: 无缓存。高玩家数场景每次 GUI 触发 O(n×m) 扫描。
- **建议**: 引入 `InventoryChangeEvent` 监听。

## P3. `GoetyPunkJeiPlugin` — 全量物品遍历

- **文件**: `GoetyPunkJeiPlugin.java:40-43`
- **问题**: `BuiltInRegistries.ITEM.stream()` 启动时全量遍历。集成包物品多时短暂卡顿。

---

# 🔵 代码异味

## S1. 未使用导入

| 文件 | 未用导入 |
|------|---------|
| `MatureEmbryoItem.java:3` | `ModEntityType` |
| `FleshCasketEvents.java:14-15` | `SpellStat`, `Optional` |
| `CrazedIllagerEntity.java:3, 36` | `BlockPos`, `BlockState` |
| `SquadBarracksMenu.java:8-9` | `Player` 重复 |

## S2. 冗余覆写

| 文件 | 问题 |
|------|------|
| `RemoteEngineBlockItem.java:68-70` | `use()` 与父类完全一致 |
| `LevelExpandedContext.java:14-15` | `tickHead()` 空方法体 |

## S3. 全限定类名代替 Import

| 文件 | 行 | 内容 |
|------|----|------|
| `CuriosHelper.java:23` | `com.goetypunk.addon.common.item.PrototypeMimicItem` | 完全限定 |
| `MimicCombinationRecipe.java:40, 64` | `com.goetypunk.addon.common.item.PrototypeItem` | 文件顶部已有 import |

## S4. `VoidBlinkSpell.java:84-86` — 静默失败

- **问题**: 范围外无目标时法术消耗灵魂但不返回任何玩家反馈。
- **建议**: `displayClientMessage()` 提示。

## S5. `MixinWallRunTravel / CollectingNeighborUpdaterMixin` — 通配符版本兼容

- **问题**: `@Inject(method = "neighborChanged*")` 通配符仅 Mixin 0.8.2+ 支持，但 `minVersion` 为 `"0.8"`。
- **影响**: 0.8.0/0.8.1 用户静默失效。

---

# 整理后的行动优先级

## 立即修复

| ID | 文件 | 问题 | 工作量 |
|----|------|------|--------|
| C1 | `FleshFurnaceBlockEntity.java:70,584` | 数组越界 + 方向错乱 | 5 分钟 |
| C2 | `TimeContext.java:15` | 缺 `@OnlyIn(Dist.CLIENT)` | 5 分钟 |

## 必须修复

| ID | 问题 | 工作量 |
|----|------|--------|
| L1-L9 | ~40 处残留调试日志 → 降级 `debug` 或移除 | 30 分钟 |
| B2, B3 | 全量实体遍历 → `getEntitiesOfClass` / 跟踪列表 | 30 分钟 |
| B4, B5 | `getShareTag` 副作用 + `deserializeNBT` 无 instanceof | 15 分钟 |

## 需验证后修复

| ID | 问题 | 需验证 |
|----|------|--------|
| G2 | `MinionSpawnHelper` 折扣逻辑 | Goety `SEHelper.decreaseSouls` 是否内部应用 `soulDiscount` |
| G3 | `TimeStopUtils` 解冻逻辑 | EndLibrary 原始行为 / 项目设计意图 |

## 渐进改进

| ID | 问题 | 类型 |
|----|------|------|
| P1-P3 | 热路径无缓存 / 全量遍历 | 性能 |
| S1-S5 | 未用导入 / 冗余 / 桩代码 | 代码质量 |
