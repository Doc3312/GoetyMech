# 蓄力跳冲刺惯性丢失

status: verified

## 问题

冲刺状态（~30 blocks/sec）下做蓄力跳，起跳后水平速度骤降至原版冲刺速度（~5.6 blocks/sec），惯性完全丢失。

## 诊断过程

### 第一轮修复（错误根因假设）

**假设**：`applyChargedJump` 中的 yaw 重建丢弃了实际 `getDeltaMovement()` 的水平分量。

```java
// 原代码
m = new Vec3(-Mth.sin(yaw) * groundSpeed, m.y, Mth.cos(yaw) * groundSpeed);
```

**修复**：直接删除 yaw 重建块，用 `getDeltaMovement()` 原样传给 `setDeltaMovement`。

**结果**：无效。水平速度仍然只有原版冲刺速度。

### 日志分析（关键转折）

从 `latest.log` 发现服务端全程：
- `isSprint=false` — 玩家不在冲刺状态
- `zza=0.0` — 无前进输入
- `hSpeed=0.0000` — 无水平速度

### 真实根因

`ServerPlayer.tick()` 在每个 tick 开始时重置 `playerInput = (0,0,false,false)`，而 C2S 包的 `setPlayerInput` 在 entity tick **之后**才被处理（`connection.tick()` 晚于 entity ticking）。因此 `aiStep()` 读到的总是清零后的输入。同时 vanilla 的 `ServerboundPlayerInputPacket` 也因为 C2SJumpChargePacket 占用了输入通道而无法正确更新 `zza`。

后果链：
1. `zza=0` → 服务端测不到前进输入
2. `player.isSprinting()` 虽然为 true（sprint packet 独立发送），但 `MOVEMENT_SPEED` modifier 虽有应用，`travel()` 中无 `zza` 输入 → 无水平加速
3. `getDeltaMovement()` 只有摩擦衰减后的微量速度
4. 任何依赖 `getDeltaMovement()` 取水平速度的方案都不成立

### 第二轮修复（正确方案）

三源取 max 策略，在 `applyChargedJump` 中：

```java
double hSpeed = Math.sqrt(m.x * m.x + m.z * m.z);          // 服务端当前 delta movement
double groundSpeed = PrototypeItem.getGroundSpeed(player);  // 客户端 charge start 实测速度
double attrSpeed = player.getSpeed();                       // MOVEMENT_SPEED attribute
if (player.isSprinting()) attrSpeed *= 1.3;                 // sprint 倍率
double bestSpeed = Math.max(hSpeed, Math.max(groundSpeed, attrSpeed));
```

当 `bestSpeed` 显著高于当前 `hSpeed` 时，用 `yaw + bestSpeed` 重建水平速度。

三个速度源互备：
- `hSpeed`: 如果服务端有速度就用它
- `groundSpeed`: 客户端 charge start 时的真实速度（最可靠，来自 clientHSpeed）
- `attrSpeed`: 从 attribute 计算的理论速度

**结果**：`PROTO_DIAG` 日志验证 `setHSpeed=1.5210`（≈ `attr 1.17 * 1.3`），后续 CJ_INJECT 每 tick 维持 `hSpeed=1.5210`，惯性完整保留。

## 涉及文件

- `PrototypeEvents.java:442` — `applyChargedJump()` 三源取 max 实现
- `PrototypeEvents.java:449` — 新增 guard condition（防止站立时 attrSpeed 产生虚假前冲）

## 时间线

| 时间 | 事件 |
|------|------|
| 2026-06-23 17:00 | 第一轮修复：删 yaw 重建 → 无效 |
| 2026-06-23 17:12 | 日志分析发现 `isSprint=false, zza=0.0` |
| 2026-06-23 17:15 | 定位输入时序根因（playerInput reset vs 包处理顺序） |
| 2026-06-23 17:30 | 第二轮修复：三源取 max → 日志验证 hSpeed=1.5210 维持 |
