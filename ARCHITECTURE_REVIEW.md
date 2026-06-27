# 架构审查报告

> 审查日期: 2026-06-27
> 审查范围: GoetyPunk 模块全部 161 个 Java 源文件
> 审查方法: 架构分层评估 + 代码质量抽样

---

## 一、总体评分

| 维度 | 评分 | 说明 |
|------|------|------|
| 包结构 | ⭐⭐⭐⭐ | 清晰的分层架构，`init/common/client` 隔离良好 |
| 注册模式 | ⭐⭐⭐⭐⭐ | 一致使用 DeferredRegister，无匿名内部类业务逻辑 |
| 代码质量 | ⭐⭐⭐ | 大部分代码规范，但有 DEBUG 混入泄漏到生产环境 |
| 可维护性 | ⭐⭐⭐⭐ | 文档完善，事件/网络/配方隔离得当 |
| 性能 | ⭐⭐⭐ | 存在明显的热路径性能问题 |
| 构建系统 | ⭐⭐⭐ | 项目名与实际架构不匹配（非 Architectury） |
| Mixin 治理 | ⭐⭐⭐ | 24 个 mixin，部分存在生产环境影响 |

**综合评级: B+（良好，但需修复关键问题）**

---

## 二、关键发现

### 🔴 严重 (Must Fix)

#### F1. DEBUG 混入泄漏到生产环境

- **位置**: `src/main/java/com/goetypunk/addon/mixin/PrototypePlayerMixin.java:21-31`
- **问题**: `gp$logAiStepHead` 和 `gp$logAiStepReturn` 在每个玩家每 tick 执行 `LOGGER.info()` 日志输出。玩家 `aiStep()` 每 tick 调用一次，意味着联机时每 tick 打印 2 行日志。
- **影响**: 日志文件膨胀、磁盘 I/O 增加、游戏性能轻微下降。更重要的是，DEBUG 信息暴露了内部状态。
- **建议**: 移除这两个注入方法，或将其包裹在 `if (LOGGER.isDebugEnabled())` 条件中，并将日志级别改为 `LOGGER.debug()`。

#### F2. `PrototypePlayerMixin` 错误的继承关系

- **位置**: `src/main/java/com/goetypunk/addon/mixin/PrototypePlayerMixin.java:15`
- **问题**: `extends LivingEntity` — Mixin 类不应有继承关系。这强制要求构造函数接收 `EntityType` 和 `Level` 参数，可能导致 Mixin 应用时的构造链问题。
- **建议**: 移除 `extends LivingEntity`，删除构造函数，使用 `@Unique` 注解声明私有字段。

#### F3. 项目名与实际架构不匹配

- **位置**: 项目根目录名 `1.20.1-Architectury-example-main`
- **问题**: 项目是纯 Forge 项目（只有一个 `build.gradle`，无 `fabric.mod.json`，无 `common/fabric/forge` 多加载器源码集），但名称中带有 "Architectury"，对贡献者和工具产生误导。
- **建议**: 重命名项目目录，或在 README 中明确标注 "Forge-only"。

---

### 🟡 中等问题 (Should Fix)

#### F4. `SquadConfigC2SPacket` 速率限制使用静态 `ConcurrentHashMap`

- **位置**: `src/main/java/com/goetypunk/addon/common/net/SquadConfigC2SPacket.java:19`
- **问题**: `lastPacketTick` 是静态 `static final` 映射，应用生命周期内持续持有所有玩家的 UUID → tick 映射，服务端永不释放。玩家退出后条目残留在内存中。
- **建议**: 使用 `@SubscribeEvent` 监听 `PlayerEvent.PlayerLoggedOutEvent` 清除条目，或将限制逻辑提取到服务端会话级别的对象中。

#### F5. 配置加载依赖次序脆弱

- **位置**: `src/main/java/com/goetypunk/addon/config/ModConfig.java:613-621`
- **问题**: `loadConfig()` 直接使用 `CommentedFileConfig` 手动加载，在 `ModLoadingContext.registerConfig()` 注册前执行。虽然当前 Forge 47.x 版本中有效，但在不同版本或环境配置下可能失败（`Cannot get config value before config is loaded` 模式）。
- **建议**: 参考 Forge 官方推荐，使用 `ModConfigEvent.Loading` 事件加载，而非手动 `CommentedFileConfig`。

#### F6. `findAllBoundFoci` 无缓存—每 tick 扫描全背包

- **位置**: `src/main/java/com/goetypunk/addon/common/item/SquadFocus.java:120-130`
- **问题**: `refreshBoundFoci()` 在每个玩家改变配置槽时对所有在线玩家调用 `findAllBoundFoci()`，后者遍历 inventory + armor + offhand 三个列表。在高玩家数服务器下，每次 GUI 交互触发 O(n×m) 扫描。
- **建议**: 引入 `ItemStack` 的 NBT 变化监听（`ItemStackListener` 或 `InventoryChangeEvent`），仅当受影响的堆栈变化时刷新。

#### F7. `goetypunk.mixins.json` 中 accessor 混入 mixins 列表

- **位置**: `src/main/resources/goetypunk.mixins.json`
- **问题**: `TimerAccessor` (client) 和 `EntityOnGroundAccessor` (mixins) 是 accessor 接口，放在 `"client"` 和 `"mixins"` 列表中。虽然技术上可行，但 Mixin 规范建议将 accessor 放在单独的 `"accessors"` 或 `"injectors"` 分区。
- **影响**: 维护时可读性降低，无法一目了然区分注入点和访问器。
- **建议**: 将 accessor 移到 `"injectors"` 数组或新建 `"accessors"` 分区（取决于 Mixin 版本支持）。

---

### 🔵 改进建议 (Nice to Have)

#### F8. 网络层协议版本硬编码

- **位置**: `src/main/java/com/goetypunk/addon/common/net/ModNetwork.java:27`
- **问题**: `PROTOCOL_VERSION = "1"` 硬编码。如果网络包结构发生不兼容变更，忘记更新版本号可能导致客户端/服务端包解码错位。
- **建议**: 定义为 `@Config` 配置项或使用构建变量注入版本。

#### F9. `CrazedIllagerEntity.doHurtTarget()` 非标准战斗模式

- **位置**: `src/main/java/com/goetypunk/addon/common/entity/CrazedIllagerEntity.java:112-132`
- **问题**: 设置 `invulnerableTime = 0` → 调用 `hurt()` → 检查生命差值 → 追加魔法伤害。`invulnerableTime` 的覆写方式与其他模组的伤害修改器可能冲突。
- **建议**: 使用 Forge 的 `LivingHurtEvent` / `LivingDamageEvent` 来实现补刀逻辑，而非在实体方法中操纵 `invulnerableTime`。

#### F10. 游戏测试覆盖不足

- **位置**: `src/main/java/com/goetypunk/addon/gametest/`
- **问题**: 全部 161 个 Java 文件仅有 2 个测试类（飞行测试 + 跑墙测试），仅覆盖 Prototype 子系统。
- **建议**: 对关键系统（点将台、血肉熔炉、幽匿束缚法术）添加 GameTest。

#### F11. Mixin 总数偏高（24 个）

- **问题**: 对于功能模组，24 个 mixin 分布在 12 个客户端 + 12 个服务端目标上。虽然每个 mixin 本身合理，但总数偏高，增加了未来 Minecraft 版本升级时的维护风险。
- **建议**: 定期审查每个 mixin 是否仍为必要；优先使用 Forge 事件系统替代 mixin。

---

## 三、架构亮点

### ✅ 清晰的包分层

```
init/         ← 纯注册，无业务逻辑
common/       ← 纯业务逻辑（item/block/entity/magic/events）
client/       ← 纯客户端代码（render/gui/HUD）
config/       ← 集中配置
compat/       ← 模组兼容（JEI/Curios/Caelus）
```

注册类与业务类严格分离，符合 "Registration → Behavior" 的职责边界。

### ✅ 优质的文档体系

- `docs/items/` 下 60+ 个物品/方块/实体文档文件
- `docs/incidents/` 记录 12 次调试经验
- `docs/agents/` 和 `AGENTS.md` 为 AI 工具提供操作上下文

### ✅ 网络层设计良好

C2S/S2C 子包分离，每个包独立 encode/decode/handle，无业务逻辑泄漏。

### ✅ 模组兼容层隔离

- Caelus 通过类反射加载实现条件注册，避免类加载崩溃
- JEI 插件独立包 `compat/jei/`
- Curios 槽位通过 data pack 标签配置

### ✅ 配置热路径缓存

- `ModConfig.refreshCache()` 在 `ModConfigEvent.Reloading` 时刷新
- `cachedLinkRange` 和 `cachedFurnaceCookingTime` 减少每 tick 配置访问开销

---

## 四、统计数据

| 指标 | 值 |
|------|-----|
| Java 源文件 | 161 |
| Mixin 总数 | 24 (12 client + 12 server) |
| 注册物品数 | 26+ |
| 注册方块数 | 6 |
| 注册实体数 | 4 |
| 注册方块实体 | 5 |
| 网络包 | 16 (8 C2S + 8 S2C) |
| 法术 (Spell) | 9 |
| 事件处理器 | 8 |
| 工具类 | 6 |
| 兼容层文件 | 9 |
| 游戏测试 | 2 |
| 文档文件 | 60+ |

---

## 五、行动建议优先级

| 优先级 | 问题 | 工作量 |
|--------|------|--------|
| P0 | F1: 移除 DEBUG 日志 mixin | 5 分钟 |
| P0 | F2: 修复 Mixin 继承 | 10 分钟 |
| P1 | F4: 网络包速率限制内存泄漏 | 15 分钟 |
| P1 | F3: 更新项目名称/README | 5 分钟 |
| P2 | F6: `findAllBoundFoci` 加缓存 | 30 分钟 |
| P2 | F5: 配置加载改事件驱动 | 20 分钟 |
| P3 | F9: 标准化战斗逻辑 | 30 分钟 |
| P3 | F7 + F8 + F10: 渐进改进 | 渐进 |
