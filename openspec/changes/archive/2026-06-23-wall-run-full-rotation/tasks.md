## 1. 工具类 — WallUtil 扩展

- [x] 1.1 新增 `findBestWallDirection(entity, range)`: 4 方向 AABB 扩展扫描，返回最近的墙面法线
- [x] 1.2 新增 `getWallFriction(entity, wallNormal)`: 获取墙面方块的摩擦系数

## 2. NBT 状态 — PrototypeItem 扩展

- [x] 2.1 新增 `getWallRunTicks` / `setWallRunTicks`: 记录跑墙持续 tick
- [x] 2.2 新增 `getWallRecoveryTicks` / `setWallRecoveryTicks`: 相机回正倒计时
- [x] 2.3 新增 `clearWallRecovery`: 清除回正状态

## 3. Config — ModConfig

- [x] 3.1 新增 `WALL_ROTATION_SPEED` (Double, default 0.15): 相机旋转插值速度
- [x] 3.2 新增 `WALL_RECOVERY_TICKS` (Int, default 10): 跑墙结束回正所需 tick
- [x] 3.3 新增 `WALL_ROTATION_LIMIT` (Double, default 85): 最大旋转角度（防止翻转）

## 4. 服务端物理 — PrototypeEvents

- [x] 4.1 `handleWallRunTick` 增加转角检测: 当前墙消失时调用 `findBestWallDirection`
- [x] 4.2 `tryStartWallRun` 增加摩擦检测: 墙面 friction ≥ 0.8 时不允许起跑
- [x] 4.3 `handleWallRunTick` 增加摩擦检测: 跑墙中移到冰面时停止
- [x] 4.4 `applyWallMovement` 微调重力方向: 将世界 -Y 投影到墙面平面
- [x] 4.5 `onPlayerTick` START 段: 允许地面冲刺激活跑墙（去掉 `!onGround()` 限制）
- [x] 4.6 `onPlayerTick` START 段: slam 状态下不允许跑墙（已有 `!isSlamming` 保护）

## 5. 客户端相机 — PrototypeRenderEvents

- [x] 5.1 新增四元数工具方法: `quatRollDeg` + `slerpWallRotation`
- [x] 5.2 重写 `updateClientWallState`: 用 `Quaternionf.rotationZ()` + SLERP
- [x] 5.3 重写 `onCameraSetup`: 从 `clientWallQuat.getRoll()` 获取 roll 并设置
- [x] 5.4 添加平滑回正: `clientWallRecoveryTicks` 倒计时 + SLERP 回 identity
- [x] 5.5 扩展 `onRenderPlayerPre`: 基于 `clientWallQuat` 的模型旋转

## 6. 网络 & 同步

- [x] 6.1 确认 C2SWallJumpOffPacket 在转角过渡后仍然有效（服务端用当前 `getWallNormal`, 客户端独立检测）
- [x] 6.2 跑墙状态 S2C 同步（可选）: 客户端独立检测墙面，不需显式 S2C 同步
