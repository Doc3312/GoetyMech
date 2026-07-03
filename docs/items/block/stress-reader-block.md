# StressReaderBlockEntity — GoetyMech

## 身份

- 类型：block-entity
- Java 类：StressReaderBlockEntity
- 关联方块：StressReaderBlock (`docs/items/block/stress-reader.md`)
- 首次创建：2026-06-14

## 设计意图

应力读取器的方块实体，读取 Create 动力网络应力数据，
与应力聚晶配合实现远程碾压轮攻击。

## Timeline

- **2026-06-27** — 降级 `[TEST]` 调试日志从 INFO 为 DEBUG。
- **2026-07-03** — 远程应力隔离（Issue ②③）：`calculateStressApplied()` 返回 0，remoteConsumption 不再注入 Reader 的 Create 网络应力；`setConsumedRemotely()` / `removeConsumer()` / `purgeStaleEntries()` 全部移除 `updateStressFor()` 调用，`consumedRemotely` 纯 Ark 簿记用。
- **2026-07-03** — 恢复 `updateStressFor()` 调用（关键修复）：`KineticNetwork.members` map 存的是 `stressPerRpm` 值，`calculateStress()` 直接从 map 读取而不调用 `calculateStressApplied()`。移除 `updateStressFor()` 后 map 值永远停留在初始值（0），network 不重算。恢复调用，但 `calculateStressApplied()` 已返回 0，每次调用正确更新 map 为 0 并触发 network recalc。
