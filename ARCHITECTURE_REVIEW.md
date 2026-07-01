# 架构审查报告（修正版）

> 审查日期: 2026-06-27 | 修正版
> 审查范围: GoetyMech 模块全部 161 个 Java 源文件
> 审查方法: 架构分层评估 + 文档交叉验证
> 文档参考: `docs/items/` (51+ 文件), `docs/incidents/` (13 文件), `docs/agents/`, `CONTEXT.md`, `PROTOTYPE_ARCHITECTURE.md`, `time-focus-pouch-architecture.md`, `openspec/` (35+ 文件)

---

## 变更记录

| 原报告问题 | 修正结果 | 依据 |
|-----------|---------|------|
| F1. DEBUG 日志 (🔴) | 🟡 **降级** | PROTOTYPE_ARCHITECTURE.md:525 声明为 DEBUG 级别（开发中），代码误用 `LOGGER.info` |
| F2. Mixin 继承关系 (🔴) | ❌ **误报，撤销** | `PrototypePlayerMixin extends LivingEntity` 是有效 Mixin 模式，为编译提供 `zza`/`jumping` 字段 |
| F3. 项目名 (🔴) | 🔵 **降级** | 目录名来自 Architectury 模板，不影响功能 |
| F9. CrazedIllager 战斗 (🔵) | ❌ **误报，撤销** | 自伤 1 HP 在 `crazed-illager.md:7` 中记录为设计决策；`invulnerableTime` 操控是常见模组战斗模式 |
| F11. Mixin 总数 (🔵) | 🔄 **修正** | 实际 25 个（13 client + 12 mixins），非 24 |

**净变化：原 11 个发现 → 修正后 8 个（3 撤销，2 降级，1 修正数字，1 新增）**

---

## 一、总体评分

| 维度 | 评分 | 说明 |
|------|------|------|
| 包结构 | ⭐⭐⭐⭐⭐ | 清晰分层，`init/common/client/config/compat` 职责明确 |
| 注册模式 | ⭐⭐⭐⭐⭐ | 一致使用 DeferredRegister，无匿名内部类业务逻辑 |
| 代码质量 | ⭐⭐⭐⭐ | 大部分代码规范，`LOGGER.info` 用于调式输出可改进 |
| 可维护性 | ⭐⭐⭐⭐⭐ | 60+ 物品文档、13 条事故记录、完整 openspec 流程 |
| 性能 | ⭐⭐⭐ | 存在明显的热路径性能问题（全量遍历） |
| 构建系统 | ⭐⭐⭐⭐ | 标准 Forge 47.x 构建，前置声明清晰 |
| Mixin 治理 | ⭐⭐⭐ | 25 个 mixin，部分存在生产环境影响 |

**综合评级: A-（良好，文档体系优秀，关键崩溃问题已识别）**

---

## 二、架构亮点

### ✅ 文档体系优秀

项目包含完整的文档层次：
- `docs/items/` 下 51 个物品/方块/实体/网络/事件文档，每个文件含身份描述 + timeline
- `docs/incidents/` 记录 13 次调试经验，含 `status: verified/disputed/superseded` 状态标记
- `docs/agents/` 为 AI 工具提供操作上下文
- `PROTOTYPE_ARCHITECTURE.md` (525 行) 详细描述原型系统设计，包含配置参数、状态 tag 总览、飞行力学公式、时序图
- `time-focus-pouch-architecture.md` (307 行) 描述时停系统架构，含完整时序图和设计决策

### ✅ 清晰的包分层

```
init/         ← 纯注册，无业务逻辑
common/       ← 纯业务逻辑（item/block/entity/magic/events）
client/       ← 纯客户端代码（render/gui/HUD）
config/       ← 集中配置，热路径缓存
compat/       ← 模组兼容（JEI/Curios/Caelus）
```

注册类与业务类严格分离，符合 "Registration → Behavior" 职责边界。

### ✅ 网络层设计良好

C2S/S2C 子包分离：8 C2S + 8 S2C 包，每个包独立 encode/decode/handle，无业务逻辑泄漏到网络层。

### ✅ 模组兼容层隔离

- Caelus 通过类反射加载实现条件注册
- JEI 插件独立包 `compat/jei/`
- Curios 槽位通过 data pack 标签配置
- Goety 仅通过 API 交互，无内部类 hack

### ✅ 配置热路径缓存

- `ModConfig.refreshCache()` 在 `ModConfigEvent.Reloading` 时刷新
- `cachedLinkRange` 和 `cachedFurnaceCookingTime` 减少每 tick 配置访问开销

### ✅ Openspec 工作流

35+ openspec 文件覆盖了 proposal → design → tasks → spec 的完整流程，已归档 6 个完整 change，活跃 4 个 change。每个 change 有独立的 `.openspec.yaml` 元数据。

---

## 三、统计修正

| 指标 | 原值 | 修正值 |
|------|------|--------|
| Mixin 总数 | 24 (12 client + 12 server) | 25 (13 client + 12 mixins) |
| 注册物品数 | 26+ | 26+ |
| 注册方块数 | 6 | 6 |
| 注册实体数 | 4 | 4 |
| 注册方块实体 | 5 | 5 |
| 网络包 | 16 (8 C2S + 8 S2C) | 16 (8 C2S + 8 S2C) |
| 事件处理器 | 8 | 8 |
| 工具类 | 6 | 6 |
| 兼容层文件 | 9 | 9 |
| 游戏测试 | 2 | 2 |
| 文档文件 | 60+ | ~80 (items 51 + incidents 13 + agents 3 + arch docs 2 + openspec 35) |

**Mixin 分类明细**：

| 分类 | 文件 | 数量 |
|------|------|------|
| 客户端（`"client"` 数组） | Blaze3DMixin, CameraMixin, ClientLevelMixin, GameRendererMixin, GuiMixin, HumanoidArmorLayerMixin, ItemInHandRendererMixin, LevelRendererMixin, MinecraftMixin, ParticleEngineMixin, TimerAccessor, TutorialMixin, EntityMixin | **13** |
| 通用/服务端（`"mixins"` 数组） | CollectingNeighborUpdaterMixin, InstantNeighborUpdaterMixin, LevelMixin, LivingEntityHurtMixin, LivingEntityTickMixin, ItemEntityPrototypeMixin, ItemEntityVoidMixin, PrototypePlayerMixin, PrototypeStepHeightMixin, PrototypeTravelMixin, MixinWallRunTravel, EntityOnGroundAccessor | **12** |

---

## 四、关键发现

### 🔴 严重 (Must Fix)

#### F1. `SquadConfigC2SPacket` 内存泄漏

- **位置**: `common/net/SquadConfigC2SPacket.java:19`
- **问题**: `static final Map<UUID, Long> lastPacketTick` 仅在速率限制时 `put`，永不 `remove`。玩家退出后 UUID 条目残留在 ConcurrentHashMap 中，随运行时间持续增长。
- **影响**: 联机服务器内存泄漏，48h+ 后可达数千个已离线玩家条目。
- **修复**: `@SubscribeEvent PlayerEvent.PlayerLoggedOutEvent` 中执行 `lastPacketTick.remove(event.getEntity().getUUID())`。

#### F2. `TEST` Logger 名污染

- **位置**: `block/entity/RemoteEngineBlockEntity.java:56`, `StressReaderBlockEntity.java:75`
- **问题**: 使用 `LoggerFactory.getLogger("TEST")` 而非 `GoetyMechMod.LOGGER`。如果 logback.xml 按 Logger 名配置过滤，`"TEST"` 名称可能绕过滤策略（如生产环境配置为 `com.GoetyMech.addon` 级别=WARN，则 `TEST` 不受影响继续输出 INFO）。
- **影响**: 日志管理失控风险。
- **修复**: 改为 `GoetyMechMod.LOGGER`。

---

### 🟡 中等问题 (Should Fix)

#### F3. 残留调试日志（~40 处 INFO 级别）

- **文件分布**:
  - `PrototypeEvents.java`（~30 处：`D START`, `PROTO_DIAG`, `SprintTick`, `SlamImpact` 等）
  - `PrototypePlayerMixin.java`（4 处：`MIXIN_AISTEP_HEAD/RETURN`，每 tick/玩家）
  - `PrototypeRenderEvents.java`（~5 处：`CLIENT_CAMERA`, `CLIENT_WALL`）
  - `CrushingWheelEntity.java`（~10 处：`[WheelTest]`）
  - `ModClientEvents.java`（2 处：`MOD_CLIENT` 误用 `LOGGER.error`）
- **文档对照**: PROTOTYPE_ARCHITECTURE.md:525 声明"PlayerTickEvent.START 还输出 zza/onGround/isFallFlying（DEBUG 级别）"。但代码使用 `LOGGER.info()` 而非 `LOGGER.debug()`。
- **建议**: 统一降级为 `LOGGER.debug()` 或移除。开发中的调试日志应使用 DEBUG 级别，生产环境默认不过滤 INFO。

#### F4. 配置加载依赖次序脆弱

- **位置**: `config/ModConfig.java:613-621`
- **问题**: `loadConfig()` 在 `ModLoadingContext.registerConfig()` 注册前使用 `CommentedFileConfig` 手动加载，绕过 Forge 配置生命周期事件。
- **文档对照**: 源码注释说明"仿照 Goety SpellConfig 的做法"。此模式在 Goety 中已验证，但依赖于 Forge 内部实现细节，跨版本可能失效。
- **建议**: 使用 `ModConfigEvent.Loading` 事件驱动加载，或保留但添加单元测试覆盖兼容性。

#### F5. `findAllBoundFoci` 无缓存

- **位置**: `SquadFocus.java:120-130`
- **问题**: 每次玩家 GUI 操作 `refreshBoundFoci()` → `findAllBoundFoci()` 遍历所有在线玩家的 inventory + armor + offhand。高玩家数服务器下，每次 GUI 交互触发 O(n×m) 扫描。
- **建议**: 引入 `InventoryChangeEvent` 监听器增量更新。

#### F6. 网络协议版本硬编码

- **位置**: `ModNetwork.java:27`
- **问题**: `PROTOCOL_VERSION = "1"` 硬编码。网络包结构变更后忘记更新可能导致 C2S/S2C 解码错位。
- **建议**: 使用构建版本注入或 `@Config` 定义。

---

### 🔵 改进建议 (Nice to Have)

#### F7. `GoetyMech.mixins.json` 中 accessor 与 injector 混合

- **位置**: `GoetyMech.mixins.json:22-35`
- **问题**: `TimerAccessor`（accessor 接口）和 `EntityOnGroundAccessor`（accessor 接口）放在 `"client"` 和 `"mixins"` 数组中，与注入点混同。Mixin 规范建议分离到 `"injectors"` 分区。
- **影响**: 可读性降低，无法一目了然区分访问器和注入点。

#### F8. 文档与代码轻微脱节

- **问题**: `time-focus-pouch-architecture.md:307` 列出了 `mixin/ServerLevelMixin.java`，但该文件不在 `src/` 中，也未在 `mixins.json` 中注册。文档记录了规划或已删除的文件。
- **影响**: 新贡献者阅读文档后可能误信该文件存在。
- **建议**: 审查并更新 `time-focus-pouch-architecture.md` 的文件清单。

#### F9. 游戏测试覆盖不足

- **位置**: `gametest/`
- **问题**: 161 个 Java 源码文件仅 2 个测试类（飞行测试 + 跑墙测试），仅覆盖 Prototype 子系统。血肉熔炉、点将台、幽匿束缚法术等无测试保护。
- **建议**: 对关键系统逐步添加 GameTest。

#### F10. Mixin 调用者模式不统一

- **位置**: `PrototypeTravelMixin.java:22, 70, 131`
- **问题**: 三个 `@Inject` 各自独立调 `PrototypeItem.hasAnyPrototype(player)`，无缓存。
- **建议**: 合并为一个前置 injector，缓存结果供后两个使用。

---

## 五、行动建议优先级

| 优先级 | 问题 | 工作量 | 类型 |
|--------|------|--------|------|
| P0 | F1: `SquadConfigC2SPacket` 内存泄漏 | 5 分钟 | 🐛 崩溃级 |
| P1 | F3: ~40 处日志降级 DEBUG | 30 分钟 | 🗑 清理 |
| P1 | F2: TEST Logger 名→改用 MOD LOGGER | 5 分钟 | 🗑 清理 |
| P2 | F5: `findAllBoundFoci` 加缓存 | 30 分钟 | ⚡ 性能 |
| P2 | F4: 配置加载改事件驱动 | 20 分钟 | 🏗 健壮性 |
| P3 | F9: 游戏测试覆盖 | 渐进 | 🧪 质量 |
| P3 | F6+F7+F8+F10: 渐进改进 | 渐进 | 🎨 代码质量 |
