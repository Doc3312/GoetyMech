# Remote Engine Stress System — Issues & Round 3 Debugging

status: disputed (③④ still broken, root cause unknown)

## 背景

RemoteEngineBlockEntity 的远程应力系统有 4 个关联 bug：

1. **HUD 应力显示 0** — `cachedConsumed = Mth.clamp(this.stress, ...)` 错误使用 this.stress（本 Create 网络应力，独立引擎恒为 0）。修复：改用 `|cachedRpm| × ratio`。✅ 已修
2. **Reader 网络过应力** — `calculateStressApplied()` 累加了 `consumedRemotely`，使 Reader 的 Create 网络应力 = 本地机器 + 远程引擎，导致过应力 crash。修复：返回 0，远程消耗纯簿记。✅ 已修
3. **拆除引擎后 Reader 仍显示 0 可用** — Reader HUD 认为可用应力为 0（或被占用），即使删除了远程引擎。**⛔ 仍有问题**
4. **引擎只有 4096 输出力** — `calculateAddedStressCapacity()` 被 Create 引擎限制在 capacity=4096。Round 2 改为 `cachedConsumed/speed`，但仍显示容量不够。**⛔ 仍有问题**

## 调试历程

### Round 1（2026-07-03a）
- 定位 `this.stress` 恒为 0 → 改用 `|rpm| × ratio`
- 效果：HUD 数据有了，但 Ark 分配循环归零

### Round 2（2026-07-03b）
- `reportConsumption()` 自动注册 entry；`load()` 设 lastSeenTick
- 消耗公式改为全量可用应力 → 消除 4096 上限（理论上）

### Round 3（2026-07-03c/e, 5 commits）
- **Step 1：远程应力隔离**
  - `StressReaderBlockEntity.calculateStressApplied()` → 返回 0
  - `setConsumedRemotely()`/`removeConsumer()`/`purgeStaleEntries()` 移除 `updateStressFor()`
  - 预期：Reader 网络不再因远程消耗过应力
  - 实际：Issue ③ 仍存在（Reader 显示 0 可用）
- **Step 2：引擎本地容量**
  - `calculateAddedStressCapacity()` → `cachedConsumed / speed`
  - 预期：引擎本地 Create 网络获得正确容量
  - 实际：Issue ④ 仍存在（4096 限制）
- **Step 3：Ark 移除 `+ consumedRemotely`**
  - `getRepresentativeRPM()` / `getTotalAvailableStress()` / `distributeConsumption()` 三处修复
  - 预期：Ark 计算可用应力 = `capacity - localStress`
  - 实际：未实际测试，OOM
- **Step 4：引擎 `refreshFromReader()` 移除 `+ consumedRemotely`**
- **Step 5：解绑移到 `useOn()`**

### 关键发现

| 观察 | 结论 |
|------|------|
| `runClient` OOM，block entities 未 tick | 无法测试，需增加 JVM heap |
| `logback-test.xml` 中 `hierarchy=DEBUG` → `[TEST]` logger 输出 WARN/ERROR 级别 | 调试日志未出现 |
| 引擎 ticklog 不出现（OOM 前） | 引擎可能未注册到 Create 网络 / BE 未 tick |

## 根因分析

**Issue ③（拆除后 Reader 0 可用）：** 可能原因：
- Step 3 中 Ark 的 `getTotalAvailableStress()` / `distributeConsumption()` 计算仍有 bug
- `consumedRemotely` 未正确清理（`removeConsumer()` 调用路径不对）
- Reader 的 `consumedRemotely` 更新后未同步到 HUD/client

**Issue ④（引擎 4096 限制）：** 即使 `calculateAddedStressCapacity()` 改为 `cachedConsumed/speed`，如果 `cachedConsumed` 在引擎 tick 前为 0，则 Create 网络发现 capacity=0 → 默认用 4096？或 `cachedConsumed` 从 Ark/Reader 读取失败。
- 一个可能性：CalculateAddedStressCapacity 的网络同步时机早于 `refreshFromReader()` / `refreshFromArk()` 的 tick → 先被 Create 用 0 capacity 注册，后面即使 refresh 也无法改变。
- 另一个可能性：`cachedConsumed` 更新后 Create 网络未触发重新计算。

## 后续

- 需 setup runClient 环境（调整 JVM 参数或换机器）
- 或进行更严格的代码审查对比 Create API 行为
- Issue ③ 和 ④ 可能共享同一根因：cachedConsumed 的 tick 时序问题
