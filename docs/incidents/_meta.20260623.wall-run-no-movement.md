# 墙跑无实际移动

status: disputed

## 问题

墙跑激活后（WALL_MV START 日志出现），玩家不感觉沿墙移动。WALL_MV 日志显示 `fwd=1.0, wallN=(1.00, 0.00, 0.00)`，但 `move` 结果仅约 0.12 m/tick 沿墙/垂直分量。

## 诊断过程

### 已验证的假设

#### 假设一：C2SWallInputPacket 未送达（已排除）

**证据**：
- 日志中 C2S 包到达时 `fwd=0.0, strafe=0.0`
- 添加了 fallback：冲刺 + 墙跑时 `forward=1.0`
- 日志确认 `fwd=1.0` 在所有 WALL_MV 行中生效

**排除依据**：forward 值正确，但仍无令人满意的移动。

#### 假设二：墙跑未激活（已排除）

**证据**：
- WALL_MV 日志持续输出（激活条件通过）
- `wallN=(1.00, 0.00, 0.00)` 有效

**排除依据**：激活逻辑正常。

#### 假设三：视线投影导致方向错误（已修复，待验证）

**修复**：冲刺时用 `down × wallN` 计算水平沿墙方向，替代 `getLookAngle()` 投影。

**预期效果**：
- 移动方向从"沿视线在墙面的投影"变为"水平沿墙方向"
- 速度 0.15 → 0.25 bpt（≈5 m/s）

**验证状态**：用户报告"什么都没变"。

### 可疑方向（待检测）

#### 怀疑 A：`player.isSprinting()` 在墙跑期间为 false

- sprint 状态可能在墙跑开始后被服务端重置
- 导致 sprint-based forward direction fallback 和 horizontal-along-wall 方向计算都不生效
- 后果：fallback 不走，forward 保持 0，方向仍用旧视线投影

**检测方案**：在 `applyWallMovement` 中加日志打印 `player.isSprinting()`、`player.zza`、`player.xxa`

#### 怀疑 B：`setDeltaMovement` 被后续 tick 逻辑覆盖

- 墙跑每 tick 设置 `player.setDeltaMovement(move)`，但后续飞行注入（CJ）/ 摩擦可能覆盖
- 需确认在 `applyWallMovement` + `return` 后，delta movement 是否被修改

**检测方案**：在 `applyWallMovement` 末尾和调用方 `return` 后各加一次日志

#### 怀疑 C：`forwardOnWall` 计算虽正确，但 move 幅度仍太小

- 即使方向正确，0.25 bpt 的沿墙速度 = 玩家半走半跑，不如冲刺快
- 需要确认用户期望的速度

## 涉及文件

- `PrototypeEvents.java:414-459` — `applyWallMovement()` 完整逻辑
- `PrototypeEvents.java:320-325` — 调用入口（live tick 中）
- `ModConfig.java:407-409` — `WALL_RUN_SPEED` 配置（0.25）
- `PrototypeRenderEvents.java` — 客户端视觉旋转（不影响移动）

## 时间线

| 时间 | 事件 |
|------|------|
| 2026-06-23 19:30 | 创建 C2SWallInputPacket + fallback → 日志确认 fwd=1.0 |
| 2026-06-23 19:45 | 发现 move 仅 0.12 m/tick，方向向上而不沿墙 |
| 2026-06-23 20:00 | 修复：sprint 时 horizontal-along-wall 方向 + 速度 0.25 |
| 2026-06-23 20:15 | 用户反馈"什么都没变" → 准备深度诊断 |
