# Stress Ark Block

应力方舟方块实体。提供基础 stress 网络功能。

## Timeline
- **2026-06-24** — 初始实现。
- **2026-07-03** — 修复引擎注册持久性：`reportConsumption()` 中自动注册被 purge 的引擎（`entry==null` 时新建）；`load()` 中 lastSeenTick 设为当前游戏时间，防止 NBT 重载后被 `purgeStaleEngines()` 立即清除。
- **2026-07-03** — 移除 `+ reader.getConsumedRemotely()`（联动 Step 1 应力隔离）：`getRepresentativeRPM()`、`getTotalAvailableStress()`、`distributeConsumption()` 三处改用纯净 `capacity - stress`，不再加回 consumedRemotely。
- **2026-07-03** — Ark 数据包增加引擎消耗：`S2CStressArkDataPacket` 新增 `engineConsumption` 字段，`syncToClient()` 传入 `getTotalConsumedByEngines()`，HUD 显示 "Eng: X SU" 绿色文本。
