## Context

当前原型的跑墙系统（PrototypeEvents.java:341-413）已有墙面检测（WallUtil AABB）、NBT 状态和基础运动学（look 投影到墙面平面）。客户端仅有 roll 倾斜（PrototypeRenderEvents.java:149-152）。要实现"墙壁=地面"的效果，需要对相机做全量旋转、转角自动过渡和平滑回正。

## Goals / Non-Goals

**Goals:**
- 跑墙时相机 up = 墙面法线方向（全量旋转，非仅 roll）
- 墙面切换时（转角）自动检测新法线并平滑过渡
- 跑墙结束时相机平滑回正到世界 up
- 冰面（slipperiness ≥ 0.8）不可跑墙
- 与现有 slam/charge-jump/flight 系统互斥

**Non-Goals:**
- 耐力系统（非跑酷模组）
- 多人同步优化（已有 C2S 包）
- 贴墙行走（仅跑墙，非悬挂/攀爬）

## Decisions

1. **相机旋转方案：ViewportEvent.ComputeCameraAngles** — 沿用现有的事件订阅，从原来只设 roll 改为设 pitch/yaw/roll。通过 Quaternion SLERP 做平滑插值。

2. **旋转计算**：用 `wall normal` 和 `world up` 的叉积为旋转轴，点积求角度，构造 Quaternion。每 client tick 从当前四元数 SLERP 到目标四元数。

3. **转角检测**：`handleWallRunTick` 中当 `getWallDirection(currentNormal)` 返回 null 时，调用 `findBestWallDirection` 做 4 方向扩展扫描，选距离最近的墙面。如果找到，设为新 normal。

4. **平滑回正**：跑墙结束后，启动 `wallRecoveryTicks` 计时器，在 10 tick 内从墙 up SLERP 回世界 up。期间 player 的正常移动不受影响。

5. **摩擦检测**：用 `level.getBlockState(blockPos).getFriction()` 判断，≥ 0.8 时不允许起跑墙。

## Risks / Trade-offs

- **客户端仅视觉效果**：相机旋转是纯客户端，服务端不知道玩家视角变了。服务端的 `applyWallMovement` 仍然用世界坐标系的 look 方向投影。这可能导致 W 键移动方向与视觉不匹配。但 ParCool 也是同样的做法，玩家可接受。
- **第三人称**：`onRenderPlayerPre` 已做模型 roll，需要扩展为全量旋转。模型旋转可能在某些视角下看起来违和。
- **SLERP 性能**：每 tick 做四元数运算开销很小（~10 ops），无性能问题。
