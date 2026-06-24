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
| 2026-06-24 | 回退 CJ-during-wall-run：恢复 isWallRunning 守卫 | 墙上跳跃与翻越翻越冲突，翻越后冲刺断掉 | PrototypeRenderEvents |
| 2026-06-24 | 新增 Kill Absorption 子系统：LivingDeathEvent 监听，击杀生物吸收 80% maxHealth 治疗玩家，溢出转 absorption（上限 maxHealth×2）；同时恢复 foodLevel（1/3 maxHealth），满后溢转 saturation | 原型缺乏击杀收益，需要战斗正向循环 | ModConfig (kill_health_absorb_ratio/kill_absorption_cap_multiplier/kill_food_restore_ratio) |
| 2026-06-24 | tryStartWallRun 加 !player.isInWater() 守卫 | 水下不应触发墙跑 | PrototypeRenderEvents |
| 2026-06-24 | 跌落自动释放阈值 3→6 格 | 3 格太容易触发 | — |
| 2026-06-25 | 同化击杀计数：LivingDeathEvent 累加 + S2C 包同步；PlayerLoggedInEvent 登录同步；LivingHurtEvent 全局伤害倍率 + indirectMagic 病毒附加伤害；能力门控（飞行始终可用，slam ≥50） | RPG 成长系统 | PrototypeItem, S2CAssimilationDataPacket |
