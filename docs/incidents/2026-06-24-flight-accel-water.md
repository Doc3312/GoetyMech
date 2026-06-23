# 飞行加速 + 水下增强 — 尝试记录

status: superseded

## 目标

1. 鞘翅飞行逐渐加速：按住W时速度从1x逐渐增至10x，松开后渐降
2. 水下氧气免疫 + 游泳速度参考冲刺加速（900%最高）
3. 水中蓄力跳

## 尝试路径

### V1：在 Mixin HEAD 内联 NBT factor 累加

将 `flightSpeedFactor`（float）存在 NBT，在 `PrototypeTravelMixin.travel() HEAD` 中：
- forward > 0.01: factor += accelRate, speed = FLIGHT_TARGET_SPEED * factor
- 仅服务端写入 NBT（`!isClientSide` guard）

**结果**：按W反而停住。根因：NBT getFloat() 默认返回 0.0，factor 从 0 起步 → speed ≈ 0 → 玩家卡住。

### V1.1：factor < 1.0 时初始化为 1.0

修复 NBT 默认值问题。

**结果**：依然无加速，甚至减速。根因不明，猜测 Mixin HEAD 的 NBT 读写与事件驱动的 sprint 模式不一致，且 client/server 各存独立 NBT 导致 desync。

### V2：对齐 sprintTick 模式 — tick计数器在 onPlayerTick.END 累加

删除 inline factor，改用 `flightAccelTick`（int）：
- `PrototypeEvents.onPlayerTick` END phase 中递增/递减（跟 sprintTick 完全一样）
- `PrototypeTravelMixin` HEAD 只读 tick → 算 `mult = 1.0 + t*(MAX-1.0)`
- 移除 FLIGHT_ACCEL_RATE/DECEL_RATE，改用 FLIGHT_ACCEL_TICKS

**结果**：用户反馈"仍然起反作用"，且游泳蓄力跳损坏。

## 关键教训

| # | 教训 |
|---|------|
| 1 | 不该在 Mixin HEAD 内直接修改 NBT。sprint 加速在事件中驱动，Mixin 只读——这个架构差异必须对齐 |
| 2 | 即使对齐了 sprintTick 模式（V2），飞行加速仍然不工作。说明问题不在计数器位置，而在更底层——可能是 `travel()` 被取消后 NBT 读值时机问题、或 client/server NBT 分裂导致 velocity desync |
| 3 | 飞行加速的 velocity-set 模型（每 tick 设 `setDeltaMovement` + `move` + `cancel`）与 sprint 的 attribute modifier 模型有本质不同。后者通过属性自然持久，前者需要确保每 tick 的读值稳定 |
| 4 | 一次性改5个文件 + 3个功能太激进。游泳和飞行互相干扰，定位困难 |
| 5 | build-run 一次循环耗时太长（~4分钟），大大拖慢调试节奏 |

## 待解答的问题

- 加速前的基础飞行已经是 velocity-set 模型（travel HEAD 接管 + cancel），它能工作说明 cancel 后的 `setDeltaMovement` 路径正确。为什么引入 `accelTick` 后同一个路径就不行了？
- 是否 `ModConfig.FLIGHT_ACCEL_TICKS.get()` 在 Mixin 上下文中访问有问题？
- 是否需要在客户端也同步 accelTick 值以避免 desync？
- 游泳蓄力跳损坏是因为 client 端 `player.onGround() || player.isInWater()` 条件导致在非预期时机发包，还是 `applyChargedJump` 中 water 分支有 bug？
