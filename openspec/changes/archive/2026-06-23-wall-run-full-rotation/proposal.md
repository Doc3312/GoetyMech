## Why

原型的跑墙系统已有骨架（墙面检测、NBT状态、基础运动学），但相机只做了倾斜（roll），玩家视角仍然是世界坐标系。要使墙壁感觉像"地面"，需要对相机做全量旋转，使相机的 up 方向对齐墙面法线，配合自动转角过渡和平滑回正，提供连贯的跑墙体验。

## What Changes

- **相机全量旋转**：跑墙时相机的 up 方向旋转到墙面法线方向，视角随着墙面变化
- **平滑过渡**：跑墙开始/结束时、转角过渡时，相机旋转使用 SLERP 插值
- **自动转角检测**：跑墙中当当前墙面结束时，自动扫描周围墙面并过渡到新的法线
- **墙面摩擦检测**：冰面等光滑方块不可跑墙（参考 ParCool）
- **平滑回正**：跑墙结束时相机从墙面 up 平滑过渡回世界 up

## Capabilities

### New Capabilities
- `wall-run-camera-rotation`: 跑墙时相机全量旋转 + 平滑插值（开始/结束/转角）
- `wall-run-transitions`: 自动转角检测与墙面法线过渡
- `wall-run-physics`: 墙面运动学微调（重力方向重映射、摩擦检测）

### Modified Capabilities

<!-- 无现有 specs 被修改 -->

## Impact

- `PrototypeRenderEvents.java`: 相机旋转逻辑重写（onCameraSetup + updateClientWallState）
- `PrototypeEvents.java`: handleWallRunTick 转角检测、applyWallMovement 微调
- `WallUtil.java`: 新增 findBestWallDirection（多方向扫描）
- `PrototypeItem.java`: 新增 wallRunTicks / isWallRecovering 等 NBT
- `ModConfig.java`: 新增 WALL_RECOVERY_TICKS 等配置
