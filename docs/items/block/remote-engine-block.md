# RemoteEngineBlockEntity — GoetyMech

## 身份

- 类型：block-entity
- Java 类：RemoteEngineBlockEntity
- 关联方块：RemoteEngineBlock (`docs/items/block/remote-engine.md`)
- 首次创建：2026-06-14

## 设计意图

遥控引擎的方块实体，实现远程动力传输（GenerateKineticBlockEntity 扩展），
包含 RPM 管理、应力计算、网络链接逻辑。

## Timeline

- **2026-06-27** — 降级 `[TEST]` 调试日志从 INFO/WARN 为 DEBUG。
- **2026-07-03a** — 修复消耗计算：`cachedConsumed` 改用 `|cachedRpm| × ratio`（`REMOTE_ENGINE_STRESS_TO_RPM_RATIO`），替代错误的 `Mth.clamp(this.stress, ...)`。`this.stress` 为 Create 网络总应力（独立引擎恒为0），导致引擎始终向 Ark/Reader 报告 0 消耗，引发 Ark 分配循环归零。添加调试日志追踪初始化/ tick / 解绑交互。
- **2026-07-03b** — 修复 Ark 引擎注册丢失：`reportConsumption()` 自动注册空 entry；`load()` 中 lastSeenTick 设为当前时间。消耗公式改为全量可用应力（`cachedConsumed = cachedArkTotalAvailable` / `trueAvailable`，替代 RPM×ratio），消除 4096 上限。
- **2026-07-03c** — 引擎本地容量修复：`calculateAddedStressCapacity()` 当 `bindingType!=NONE && cachedConsumed>0` 时返回 `cachedConsumed/speed`，使引擎本地 Create 网络容量等于远程消耗量，消除 4096 上限（Issue ④）。
- **2026-07-03d** — `refreshFromReader()` 移除 `+ reader.getConsumedRemotely()`（与 Step 1 应力隔离联动）。
- **2026-07-03e** — 新增 `clearLinkedPos()` 方法：清除 binding/linkedPos/linkedDim/arkPos，从 Reader 移除消费，重置 cached 值，支持完整解绑。
