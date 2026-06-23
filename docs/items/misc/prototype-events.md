# PrototypeEvents — goetypunk

## 身份

- 类型：misc (Forge event subscriber，服务端)
- Java 类：PrototypeEvents
- 首次记录：2026-06-24

## 设计意图

Prototype 系统服务端核心逻辑，管理 4 个子系统：

| 子系统 | 事件驱动 | 说明 |
|--------|---------|------|
| **Wall-run** | livingTick → `tryStartWallRun` / `handleWallRunTick` / `handleWallJumpOff` | 墙面检测 + 吸附/角落转移/跳离 |
| **Charge jump** | C2S 包 → `handleJumpChargeFromClient` → `applyChargedJump` | 蓄力高度反算初速度，水平惯性 + 扣跳 |
| **Slam** | C2S 包触发 → `enterSlamState` / `handleSlamTick` / `triggerSlamImpact` | 悬停→自由落体→地面冲击 AoE 伤害 + 碎块 ring 动画 |
| **Sprint** | playerTick → `Attributes.MOVEMENT_SPEED` 修饰器 | 随时间线性加速到 `SPRINT_MAX_MULTIPLIER` |

辅助机制：坠落免伤 (`LivingFallEvent`)，slam 无敌帧 (`LivingAttackEvent`)，延迟 AoE 伤害 + 升空冲击波，多级环状碎块弹出动画。

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 首次文档记录 | 还清文档债务 | PrototypeItem, WallUtil, PrototypeRenderEvents |
