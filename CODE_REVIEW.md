# 全员代码审查报告

> 审查日期: 2026-06-27
> 审查范围: 全部 161 个 Java 源文件
> 审查方法: 按包分区多路并行审查 + 手工验证

---

## 发现总览

| 等级 | 计数 | 说明 |
|------|------|------|
| 🔴 崩溃级 | 4 | 运行时必崩或资源彻底不可用 |
| 🔴 玩法级 | 3 | 逻辑错误导致游戏行为严重偏离设计 |
| 🟡 日志污染 | ~40 处 | INFO 级别残留调式日志，生产环境日志刷屏 |
| 🟡 逻辑缺陷 | 8 | NPE 风险/数据不同步/配置不生效 |
| 🟡 性能 | 5 | 热路径全量遍历/内存泄漏的可能 |
| 🔵 代码异味 | ~12 | 冗余覆写/未用导入/全限定类名/桩代码 |

---

# 🔴 崩溃级

## C1. `FleshFurnaceBlockEntity.getCapability()` — 数组越界崩溃

- **文件**: `src/main/java/com/goetypunk/addon/common/block/entity/FleshFurnaceBlockEntity.java:584`
- **问题**: `SidedInvWrapper.create(this, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)` 数组长度为 5（**不含 DOWN**）。但 `handlers[side.get3DDataValue()]` 中 EAST 的索引为 5（超出 `int[5]` 范围）→ `ArrayIndexOutOfBoundsException`。
- **任何对 FleshFurnace EAST 面的自动化（漏斗、管道）操作**都会直接崩溃。
- **修复**: `SidedInvWrapper.create(this, Direction.values())` 传 6 个方向，或映射索引。

## C2. `HumanoidArmorLayerMixin` — Mixin 静默失效

- **文件**: `src/main/java/com/goetypunk/addon/mixin/client/HumanoidArmorLayerMixin.java`
- **Config**: `goetypunk.mixins.json:13` 中条目为 `"HumanoidArmorLayerMixin"`，base package = `com.goetypunk.addon.mixin`
- **实际包**: `com.goetypunk.addon.mixin.client`
- **结果**: Mixin 加载器查找 `com.goetypunk.addon.mixin.HumanoidArmorLayerMixin`，文件在 `client` 子包中。**该 Mixin 永远不会被应用**（升级到 0.8.4 以上会抛 Warn）。拟态盔甲渲染功能完全不可用。
- **修复**: 条目改为 `"client.HumanoidArmorLayerMixin"`。

## C3. `SquadBarracksScreen` 客户端/服务端物品不同步

- **文件**: `src/main/java/com/goetypunk/addon/client/gui/SquadBarracksScreen.java:139-184`
- **问题**: `mouseClicked` 返回 `true` 阻止了默认容器点击包的发送。服务端收到 `SquadConfigC2SPacket` 后**不会处理物品消耗**。玩家在 GUI 中消耗的刷怪蛋在服务端未被消费，重新打开 GUI 后刷怪蛋仍在。
- **影响**: 可重复刷物品（刷怪蛋复制漏洞）。
- **修复**: 在 C2S 包中新增槽位标识，由服务端处理消耗逻辑。

## C4. `TimeContext.safeClientFrameTime()` 缺少 `@OnlyIn`

- **文件**: `src/main/java/com/goetypunk/addon/common/time/TimeContext.java:15-17`
- **问题**: 方法直接调用 `Minecraft.getInstance().getFrameTime()` 但**没有任何 `@OnlyIn(Dist.CLIENT)` 注解**。在 Dedicated Server 上调用即抛 `java.lang.RuntimeException: attempted to call Minecraft during mixin apply`。
- **修复**: 加 `@OnlyIn(Dist.CLIENT)` 或拆分为客户端/服务端双实现。

---

# 🔴 玩法级

## G1. `CrazedIllagerEntity` / `CrazedIllagerServant` — 每次攻击对自己造成 1.0 伤害

- **文件**:
  - `src/main/java/com/goetypunk/addon/common/entity/CrazedIllagerEntity.java:127`
  - `src/main/java/com/goetypunk/addon/common/entity/CrazedIllagerEntity.java`（定位具体行号需查看 `CrazedIllagerServant` 的对应代码）
- **问题**: `doHurtTarget()` 方法最后调用 `this.hurt(this.damageSources().generic(), 1.0F);`。疯狂刌民攻击频率为每 4 tick，15 点血量在约 3 秒内自杀。每次攻击自己扣 1 点血 ≈ 6.7% 最大生命值。
- **影响**: 疯狂刌民约 3 秒自杀，作为敌对生物或仆从都完全没有存在价值。
- **修复**: 去除自伤逻辑，或改为有冷却/有条件触发。

## G2. `MinionSpawnHelper.java` — 灵魂折扣计算后未使用

- **文件**: `src/main/java/com/goetypunk/addon/common/utils/MinionSpawnHelper.java:165-179`
- **问题**:
  ```java
  float discount = SEHelper.soulDiscount(player);
  int effectiveCost = Math.max(1, (int) (soulCost * discount)); // 计算了折扣价
  ...
  SEHelper.decreaseSouls(player, soulCost);  // ⚠ 扣的还是原始价格！
  ```
  `effectiveCost` 从未被使用。两处 `decreaseSouls` 调用都传了原始的 `soulCost`。Goety 的灵魂折扣系统对此模块完全不生效。
- **影响**: 所有 Goety 的灵魂折扣物品/附魔对此模块无效，玩家付全价。

## G3. `HumanoidArmorLayerMixin` — 附魔闪光渲染参数错位

- **文件**: `src/main/java/com/goetypunk/addon/mixin/client/HumanoidArmorLayerMixin.java:63-70`
- **问题**: `ItemRenderer.getArmorFoilBuffer(buffer, renderType, glint, isItem)` 传参错位：
  - 第 64 行：`stack.hasFoil()` 传给了 `isItem` 而非 `glint` → 首次渲染永远无闪光
  - 第 68-70 行：无论物品是否附魔，始终渲染两层闪光 → 无附魔物品的盔甲也发光
- **影响**: 拟态盔甲渲染效果完全不符合预期。

---

# 🟡 日志污染

> 以下文件包含 `LOGGER.info()` / `LOGGER.warn()` 级别的调式日志，在生产环境中会在每 tick/每次触发时输出，**严重污染日志文件**。

## L1. `PrototypeEvents.java` — ~30 处 INFO 日志

**所有 `LOGGER.info` 调用均为调式残留**，包括但不限于：

| 行号 | 内容 |
|------|------|
| 261 | `LOGGER.info("D START: ...")` |
| 281 | `LOGGER.info("PROTO_DIAG START: ...")` |
| 287 | `LOGGER.info("Prototype ground takeoff: ...")` |
| 292 | `LOGGER.info("Prototype mid-air deployment: ...")` |
| 303 | `LOGGER.info("D POST_TRAVEL: ...")` |
| 331 | `LOGGER.info("SprintTick: ...")` |
| 342 | `LOGGER.info("SprintMod AFTER: ...")` |
| 369 | `LOGGER.info("PROTO_DIAG CJ_CLEAR: ...")` |
| 377 | `LOGGER.info("PROTO_DIAG CJ_INJECT: ...")` |
| 389 | `LOGGER.info("D END: ...")` |
| 427 | `LOGGER.info("Prototype wall-run LIFT: ...")` |
| 429 | `LOGGER.info("Prototype wall-run START: ...")` |
| 455 | `LOGGER.info("Prototype wall-run CORNER: ...")` |
| 562 | `LOGGER.info("Sprint inertia: ...")` |
| 582, 593 | `LOGGER.info("PROTO_DIAG applyChargedJump/TapJump: ...")` |
| 694 | `LOGGER.info("SlamImpact: ...")` |
| 735, 746, 757, 763 | `LOGGER.info("AoE damage / SKIP / HIT: ...")` |
| 881 | `LOGGER.info("AoE particle: ...")` |
| 944, 958, 976 | `LOGGER.info("ring[{ }] ...")` |

## L2. `PrototypePlayerMixin.java` — 4 处每 tick INFO 日志

- **文件**: `src/main/java/com/goetypunk/addon/mixin/PrototypePlayerMixin.java:23-44`
- `MIXIN_AISTEP_HEAD` / `MIXIN_AISTEP_RETURN` — 每个玩家每 tick 打两行，联机 10 人 = 每秒 400 行日志。

## L3. `PrototypeRenderEvents.java` — 每帧 INFO 日志

- **文件**: `src/main/java/com/goetypunk/addon/client/PrototypeRenderEvents.java:213-218, 335-337`
- `LOGGER.info("CLIENT_CAMERA: rollDeg=...")` 和 `LOGGER.info("CLIENT_WALL: sprint=...")` — 每帧记录。

## L4. `RemoteEngineBlockEntity.java` — 7 处 [TEST] 前缀日志

- **文件**: `src/main/java/com/goetypunk/addon/common/block/entity/RemoteEngineBlockEntity.java:56-169`
- 使用 `LoggerFactory.getLogger("TEST")`，所有输出带 `[TEST]` 前缀。

## L5. `StressReaderBlockEntity.java` — 3 处 [TEST] 前缀日志

- **文件**: `src/main/java/com/goetypunk/addon/common/block/entity/StressReaderBlockEntity.java:24-110`
- 同样使用 `LoggerFactory.getLogger("TEST")`。

## L6. `CrushingWheelEntity.java` — ~10 处 [WheelTest] 前缀日志

- **文件**: `src/main/java/com/goetypunk/addon/common/entity/CrushingWheelEntity.java:55-163`
- 大量 `LOGGER.info("[WheelTest] ...")` 和 `LOGGER.debug(...)`。

## L7. 其他文件

| 文件 | 问题 |
|------|------|
| `CrushingWheelSpell.java:131-132` | `LOGGER.info("StressFocus spell → ...")` |
| `ModClientEvents.java:43, 61` | `LOGGER.error()` 用于正常初始化事件，级别错误 |
| `ModClientForgeEvents.java:58, 79, 133` | INFO 级调试日志 |
| `AssimilationSaveManager.java:35` | INFO 级，改为 DEBUG 更合理 |

---

# 🟡 逻辑缺陷

## B1. `TimeStopUtils.java` — 停止时停时计数器清零对象反转

- **文件**: `src/main/java/com/goetypunk/addon/common/time/TimeStopUtils.java:55-63`
- **问题**: 在 `!z`（停止时停）分支中，当发现其他实体仍有 `getTimeStopCount > 0` 时，代码将 **来源者**（`source`）的计数器清零而非**该实体**（`living`）：
  ```java
  if (force) TimeStopEntityData.setTimeStopCount(source, 0);  // → 应传 living
  ```
- **影响**: 多人时停环境下，一个玩家的时停结束会错误地清掉另一个正在时停的玩家的计时器。

## B2. `FleshCasketScreen` — 标题 Y 坐标未设置

- **文件**: `src/main/java/com/goetypunk/addon/client/gui/FleshCasketScreen.java:22`
- **问题**: 设置了 `titleLabelX` 但未设置 `titleLabelY`（依赖父类默认值 6），不同 GUI 纹理高度下可能偏移。

## B3. `CrazedIllagerEntity` — AI 目标优先级冲突

- **文件**: `src/main/java/com/goetypunk/addon/common/entity/CrazedIllagerEntity.java:77-78`
- **问题**: 两个 `NearestAttackableTargetGoal` 都使用优先级 `3`，导致选择行为不确定。

## B4. `CrazedSummonSpell` / `SquadSpell` — 全量实体遍历

- **文件**:
  - `src/main/java/com/goetypunk/addon/common/magic/spell/CrazedSummonSpell.java:76`
  - `src/main/java/com/goetypunk/addon/common/magic/spell/SquadSpell.java:91`
- **问题**: `worldIn.getAllEntities()` 遍历世界所有实体。在大型世界或时停场景下开销巨大。应使用 `getEntitiesOfClass(IOwned.class, aabb)` 过滤。

## B5. `TimeStopUtils.java` — 全量 LivingEntity 遍历

- **文件**: `src/main/java/com/goetypunk/addon/common/time/TimeStopUtils.java:55-56`
- **问题**: `AABB(0,0,0).inflate(30000000)` = 6000 万方块直径立方体，等价于 `getEntitiesOfClass(LivingEntity.class, ...)` 遍历所有加载的活体实体。每次时停触发都有此开销。

## B6. `FleshCasketItem.java` — `getShareTag` 副作用修改源 NBT

- **文件**: `src/main/java/com/goetypunk/addon/common/item/curio/FleshCasketItem.java:99`
- **问题**: `getShareTag()` 调用时将 `cap` 键写入 `stack` 的 NBT（修改了源 ItemStack），违反了 getter 无副作用预期。
- **修复**: 用 `stack.getTag().copy()` 创建副本操作。

## B7. `TimeStopRandom.java` — 字段遮蔽

- **文件**: `src/main/java/com/goetypunk/addon/common/time/TimeStopRandom.java:7-8`
- **问题**: `AtomicLong seed` 遮蔽了父类 `LegacyRandomSource` 的 `seed` 字段。父类的 `seed` 在构造后**从未初始化**（值为 0），如果父类的其他方法直接读自己私有的 seed，将得到错误结果。

## B8. `RemoteEngineBlockItem.java` — NPE 风险

- **文件**: `src/main/java/com/goetypunk/addon/common/item/RemoteEngineBlockItem.java:94-102`
- **问题**: `getBoundPos()` 和 `getBoundDim()` 中 `stack.getTag()` 可能为 null，直接解引用抛 NPE。
- **相同模式也存在于**: `SquadFocus.java:128` (`offhand.get(0)` 越界风险)。

---

# 🟡 性能问题

## P1. 热路径上重复 `hasAnyPrototype` 检查

- **文件**: `src/main/java/com/goetypunk/addon/mixin/PrototypeTravelMixin.java:22, 70, 131`
- **问题**: 三个 `@Inject` 各自独立调用 `PrototypeItem.hasAnyPrototype(player)`。对非原型玩家，三个 injector 都执行守卫判断后返回。建议用单个前置 injector 缓存结果。

## P2. `findAllBoundFoci` 无缓存

- **文件**: `src/main/java/com/goetypunk/addon/common/item/SquadFocus.java:120-130`
- **问题**: `refreshBoundFoci()` 对每个在线玩家遍历 inventory + armor + offhand。高玩家数服务器下每次 GUI 交互触发 O(n×m) 扫描。

## P3. `FleshFurnaceBlockEntity. Math.abs` 哈希偏移潜在溢出

- **文件**: `src/main/java/com/goetypunk/addon/common/block/entity/FleshFurnaceBlockEntity.java:84`
- **问题**: `Math.abs(Integer.MIN_VALUE)` 仍为负数（`Integer.MIN_VALUE == -2147483648`）。极罕见情况下 `scanOffset` 为负，熔炉扫描永远无法触发。

## P4. `GoetyPunkJeiPlugin` — 启动时全量物品遍历

- **文件**: `src/main/java/com/goetypunk/addon/compat/jei/GoetyPunkJeiPlugin.java:40-43`
- **问题**: `BuiltInRegistries.ITEM.stream()` 全量遍历。集成包物品多时会短暂卡顿。

---

# 🔵 代码异味

## S1. 未使用导入

| 文件 | 行 | 未使用导入 |
|------|----|-----------|
| `MatureEmbryoItem.java` | 3 | `com.Polarice3.Goety.common.entities.ModEntityType` |
| `FleshCasketEvents.java` | 14, 15 | `SpellStat`, `Optional` |
| `CrazedIllagerEntity.java` | 3, 36 | `BlockPos`, `BlockState` |
| `SquadBarracksMenu.java` | 8-9 | `Player` 重复导入 |
| `PrototypeEvents.java` | 22, 33 | `AABB` 重复导入 |

## S2. 冗余覆写

| 文件 | 行 | 问题 |
|------|----|------|
| `RemoteEngineBlockItem.java` | 68-70 | `use()` 与父类完全一致 |
| `LevelExpandedContext.java` | 14-15 | 空方法体桩代码 |

## S3. 全限定类名代替 Import

| 文件 | 行 | 全限定类名 |
|------|----|-----------|
| `CuriosHelper.java` | 23-24 | `com.goetypunk.addon.common.item.PrototypeMimicItem` |
| `MimicCombinationRecipe.java` | 40, 64 | `com.goetypunk.addon.common.item.PrototypeItem`（文件顶部已有 import！）|

## S4. `PrototypeItem.hasFlight(int kills)` — 参数未使用

- **文件**: `src/main/java/com/goetypunk/addon/common/item/PrototypeItem.java:381-383`
- 永远 `return true`，参数 kills 无意义。疑似设计半成品。

## S5. `FleshCasketItemCapability.deserializeNBT` — 无 instanceof 检查

- **文件**: `src/main/java/com/goetypunk/addon/common/item/capability/FleshCasketItemCapability.java:48`
- **问题**: `(CompoundTag) nbt` 无 `instanceof` 检查。

## S6. `ModConfig` 分类名与常量名不一致

- **文件**: `src/main/java/com/goetypunk/addon/config/ModConfig.java:582`
- `BUILDER.push("chase_focus")` 但常量为 `VOID_BLINK_*`。

## S7. `C2STimeStopPacket` 硬编码 180 tick

- **文件**: `src/main/java/com/goetypunk/addon/common/net/c2s/C2STimeStopPacket.java:28`
- 硬编码 `180` 而非使用 `ModConfig.TIME_STOP_DEFAULT_DURATION.get()`。

## S8. `MixinWallRunTravel` 通配符方法名版本兼容

- **文件** (分析与 CollectingNeighborUpdaterMixin 一致):
  `@Inject(method = {"shapeUpdate", "neighborChanged*", ...})` — 通配符 `*` 仅 Mixin 0.8.2+ 支持，配置文件 `minVersion` 为 `"0.8"`。使用 0.8.0/0.8.1 时静默失效。

---

# 总结优先级

## 立即修复（崩溃）

| ID | 等级 | 文件 | 简述 | 工作量 |
|----|------|------|------|--------|
| C1 | 🔴 | `FleshFurnaceBlockEntity.java:584` | EAST 面数组越界崩溃 | 5 分钟 |
| C2 | 🔴 | `HumanoidArmorLayerMixin` 包路径错误 | Mixin 永不生效 | 2 分钟 |
| C3 | 🔴 | `SquadBarracksScreen.java` | 客户端/服务端不同步物品复制 | 30 分钟 |
| C4 | 🔴 | `TimeContext.java:15` | Dedicated Server 闪崩 | 5 分钟 |

## 必须修复（玩法）

| ID | 等级 | 文件 | 简述 | 工作量 |
|----|------|------|------|--------|
| G1 | 🔴 | `CrazedIllagerEntity.java:127` | 每次攻击自伤 1 点，3 秒自杀 | 10 分钟 |
| G2 | 🔴 | `MinionSpawnHelper.java:165-179` | 灵魂折扣计算后未使用 | 5 分钟 |
| G3 | 🔴 | `HumanoidArmorLayerMixin.java:63-70` | 闪光渲染参数传反 | 10 分钟 |

## 清理（日志）

| ID | 等级 | 文件 | 简述 | 工作量 |
|----|------|------|------|--------|
| L1-L7 | 🟡 | 9 个文件 ~40 处 | INFO 级调式日志 → 移除或改 DEBUG | 30 分钟 |

## 优化（逻辑）

| ID | 等级 | 文件 | 简述 | 工作量 |
|----|------|------|------|--------|
| B1 | 🟡 | `TimeStopUtils.java:55-63` | 计数器清零目标反转 | 5 分钟 |
| B2-B8 | 🟡 | 多文件 | NPE / 性能 / 字段遮蔽 / 全量遍历 | 渐进 |
| P1-P4 | 🟡 | 多文件 | 热路径无缓存 / 全量遍历 | 渐进 |
| S1-S8 | 🔵 | 多文件 | 代码异味 / 冗余 / 不一致 | 渐进 |
