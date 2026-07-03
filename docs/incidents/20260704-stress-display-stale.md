# 应力显示不更新 — 标记为失败/补债

status: disputed

## 问题

引擎/Reader/Ark 显示的压力/容量数值在拆掉机器后不更新。机器确实能转（功能正确），但所有 HUD 数值卡在旧值（引擎显示红色 14w/0，Reader/机器显示 0/14w）。

## 调试过程

1. **INVALIDATE 重建后应力不恢复的初步诊断**：引擎被销毁重建后 `getTheoreticalSpeed()` 永远返回 `this.speed`（64），不受 `overStressed` 影响。`calculateStress()` 用 `getTheoreticalSpeed()` 算应力 → 永不下降。

2. **`updateGeneratedRotation()` 的 `prevSpeed == speed` 短路**：引擎加载后 `this.speed`（64）= `getGeneratedSpeed()`（64），`applyNewSpeed()` 不执行，但 `hasNetwork() && speed != 0` 分支仍执行，stress/capacity 会更新。

3. **`setConsumedRemotely()` → `NETWORK_UPDATED` 标志缺失**：Reader 的 `setConsumedRemotely()` 虽然调了 `updateStressFor()`，但 `StressReaderBlockEntity.NETWORK_UPDATED` 静态标志**没有任何地方设为 true**。Ark 依赖此标志触发 `recomputeIfNeeded()`，导致 Ark 侧最多有 20tick 延迟。

4. **根因：Reader 和机器在**不同的 Create 网络**中**。Reader 的 `this.stress` / `this.capacity` 来自 Reader 自己的 Create 旋转网络（独自或只有少量辅助方块）。机器在另一个独立网络中。机器拆掉后机器网络应力降低，但 Reader 网络的应力**不变**。Ark 的 `getRawAvailableStress() = Σ(capacity - stress)` 永远不变。

5. **结论**：这是一个跨网络数据桥接的架构问题，不是简单的 stress propagation bug。当前 Reader+Ark 系统的应力数据来源是 Reader 自身网络的 stress ➜ 与机器网络完全隔离。

## 标记为失败的原因

- 修复需要引入跨网络的应力数据查询/桥接，改动较大，当前优先级不足
- 功能正常（机器能转、应力限制生效），仅 HUD 显示 stale
- 补债项：未来需建立一个机制让 Reader 能读到机器网络的实际应力

## 技术教训

- Create 的 `KineticNetwork` 按物理旋转连接划分，Reader 必须接入机器网络才能自动获取其应力
- 跨网络获取应力需要：A) Reader 连线入网；B) 或显式 data bridge（如引擎 `refreshFromArk()` 直接读机器网络的 `currentStress`）
- `NETWORK_UPDATED` 标志是一个已知的设计遗漏，后续若做 Reader-Ark 实时同步应补上
- `getTheoreticalSpeed()` vs `getSpeed()` 的区别是 `calculateStress()` vs `calculateCapacity()` 分别使用不同速度源的根源

## 相关文件

- `src/main/java/com/goetymech/addon/common/block/entity/StressReaderBlockEntity.java`
- `src/main/java/com/goetymech/addon/common/block/entity/StressArkBlockEntity.java`
- `src/main/java/com/goetymech/addon/common/block/entity/RemoteEngineBlockEntity.java`
- `D:\Libcode\Create-mc1.20.1-dev\Create-mc1.20.1-dev\src\main\java\com\simibubi\create\content\kinetics\KineticNetwork.java`
- `D:\Libcode\Create-mc1.20.1-dev\Create-mc1.20.1-dev\src\main\java\com\simibubi\create\content\kinetics\base\KineticBlockEntity.java`
- `D:\Libcode\Create-mc1.20.1-dev\Create-mc1.20.1-dev\src\main\java\com\simibubi\create\content\kinetics\base\GeneratingKineticBlockEntity.java`
