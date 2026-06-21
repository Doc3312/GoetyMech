# 跑墙物理修复记录

## 问题：墙跑玩家不下滑，以正常重力速度坠落

**根因**：`applyWallMovement()` 在 `PlayerTick.END` phase 执行，设置的 velocity 是下个 tick `travel()` 的起点。原代码继承玩家当前速度的 Y 分量（-0.08/tick），乘摩擦 + 加下滑量后 Y 仍很大，玩家以重力速度坠落。

## 修复方案（`StormWingEvents.java`）

### 1. Y 轴直接控制，不继承下落速度（`applyWallMovement`）

```java
// 水平分量乘摩擦，Y 独立设为慢滑速度
surfaceVel = new Vec3(surfaceVel.x * WALL_FRICTION,
        -WALL_SLIDE_SPEED,
        surfaceVel.z * WALL_FRICTION);
// adhesionVec.y = 0（墙法线水平），最终 Y = -WALL_SLIDE_SPEED
player.setDeltaMovement(surfaceVel.x + adhesionVec.x,
        surfaceVel.y,
        surfaceVel.z + adhesionVec.z);
```

**时序推理**：
- Tick N END：Y = `-WALL_SLIDE_SPEED`（设进 velocity）
- Tick N+1 `travel()`：读取 Y = `-WALL_SLIDE_SPEED`，`move()` 以此值执行
- `travel()` 在 `move()` 后执行 `deltaMovement.add(0, -0.08, 0)`，但该值被 N+1 END 覆盖
- 稳态：每 tick 玩家以 `-WALL_SLIDE_SPEED` 速度下滑

### 2. 视线方向保留 Y 分量，支持仰头爬墙（`applyWallMovement`）

```java
// 原代码：去除 Y，只水平移动
Vec3 lookH = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z);

// 新代码：保留 Y，仰头向上爬、低头向下滑
Vec3 lookH = player.getLookAngle();
```

`forwardOnWall = projectOnPlane(lookH, wallN)` 保留 Y 后：
- 仰头（`lookAngle.y > 0`）→ `forwardOnWall.y > 0` → W 带玩家沿墙向上
- 平视（`lookAngle.y ≈ 0`）→ `forwardOnWall.y ≈ 0` → W 水平跑墙
- 低头（`lookAngle.y < 0`）→ `forwardOnWall.y < 0` → W 带玩家沿墙下滑

### 3. 非继承的参数体系

`surfaceVel` 从 `projectOnPlane(vel, wallN)` 获得当前沿墙速度（XZ + Y），然后：
1. 加 `inputAccel`（基于 `MOVEMENT_SPEED` 属性，水平方向）
2. 水平分量乘 `WALL_FRICTION`，Y 直接设 `-WALL_SLIDE_SPEED`
3. 加 `adhesionVec`（`wallN * WALL_ADHESION`，纯水平拉向墙面）

不继承任何下落动量，墙跑是独立运动状态。

### 4. 退出条件（`handleWallRunTick`）

```java
// 当前：!isSprinting() → 不冲刺就退出（旧版）
// 应改为：player.onGround() → 落地才退出（新版）
player.onGround() || player.isShiftKeyDown()
    || player.isInWater() || player.isInLava()
    || player.isFallFlying()
```

## 配置项重命名对照

| 旧名 | 新名 | 用途 | 默认值 |
|------|------|------|--------|
| `WALL_RUN_SPEED` | `WALL_FRICTION` | 水平速度阻尼（越小越滑） | 0.8 |
| `WALL_GRAVITY` | `WALL_SLIDE_SPEED` | Y 轴慢滑速度（/tick） | 0.005 |
| `WALL_PULL_SPEED` | `WALL_ADHESION` | 吸附拉向墙面力度 | 0.5 |
| `WALL_JUMP_OFF_VELOCITY` | — | 删除（墙面跳走被蓄力跳替代） | — |

## 已删除功能

- `STORED_ZZA` 字段 + `gp$captureZza` mixin（不再需要飞行加速）
- `handleWallJumpOff` + `C2SWallJumpOffPacket` + 网络注册 + 客户端发送（墙面蓄力跳替代）
- 地面起飞 + 空中强制飞行（`onPlayerTick` START phase）
- 飞行加速/飞行 tick 追踪（`onPlayerTick` END phase）
- `getFlightTick` / `setFlightTick` / `getLastBoostTick` / `setLastBoostTick`（`StormWingItem`）
- `BOOST_ACCELERATION` / `FLIGHT_BOOST_STRENGTH` / `FLIGHT_BOOST_COOLDOWN` / `BOOST_ACCELERATION`（`ModConfig`）

## 当前已知未解决

- 文件被 Git 回退到初始提交，上述所有修改在硬盘上不存在
- 需重新执行 8 个文件的修改（`StormWingEvents.java`、`ModConfig.java`、`StormWingItem.java`、`ModNetwork.java`、`StormWingRenderEvents.java`、`StormWingPlayerMixin.java`）+ 删除 `C2SWallJumpOffPacket.java`
