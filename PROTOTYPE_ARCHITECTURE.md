# 原型 (Prototype) 架构说明

## 概述

原型是基于 Goety 模组的鞘翅类物品，支持 **胸甲槽** 和 **Curios 背部/身体槽**。核心玩法为 **飞行→猛击→二次延迟魔法伤害** 双阶段循环，附带 **爬墙系统**、**跳跃蓄力** 和 **地面冲刺加速**。

## 文件结构

```
src/main/java/com/GoetyMech/addon/
├── common/
│   ├── item/
│   │   ├── PrototypeItem.java              # 物品定义 + PersistentData tag 静态访问器
│   │   └── curio/PrototypeCurio.java       # Curios 能力附加器（curioTick / canEquip）
│   ├── events/
│   │   └── PrototypeEvents.java            # 核心逻辑：slam 状态机、爬墙、蓄力、环动画、伤害
│   ├── utils/
│   │   ├── CuriosHelper.java               # Curios 槽位检测工具
│   │   └── WallUtil.java                   # 爬墙几何工具（墙面检测、向量投影）
│   ├── net/
│   │   ├── ModNetwork.java                 # 网络通道注册
│   │   ├── c2s/C2SCancelSlamPacket.java    # 客户端→服务端：取消猛击
│   │   ├── c2s/C2SJumpChargePacket.java    # 客户端→服务端：跳跃蓄力 tick
│   │   ├── c2s/C2SFlightInputPacket.java   # 客户端→服务端：飞行 W 键输入 (forward 0/1)
│   │   └── s2c/S2CSlamImpactPacket.java    # 服务端→客户端：冲击粒子+音效
│   └── config/ModConfig.java               # 全部配置项
├── client/
│   └── PrototypeRenderEvents.java          # 客户端：翅翼渲染、爬墙镜头/模型倾斜、按键检测
├── compat/caelus/
│   └── CaelusFlightHandler.java            # Caelus 兼容：Curios 背部槽时添加飞行 attribute
└── mixin/
    ├── PrototypeTravelMixin.java           # Player.travel() 双注入：飞行 velocity-set + TAIL 漂移
    └── PrototypePlayerMixin.java           # 阻止蓄力时触发 Player.jumpFromGround()
```

## 核心架构

### 1. 物品定义

`PrototypeItem extends ElytraItem`：

- `canElytraFly()` → 恒 true
- `elytraFlightTick()` → slam 时返回 false（强制停飞），否则 true
- `isEnchantable()` → false（不可附魔）
- **所有状态存于 entity PersistentData**，而非物品 NBT

#### 状态 tag 总览（全部为静态方法，通过 entity 读写）

| Tag | 访问器 | 用途 |
|-----|--------|------|
| `GoetyMech.slamming` | `isSlamming/setSlamming` | 猛击状态标志 |
| `GoetyMech.slam_tick` | `getSlamTick/setSlamTick` | 猛击已持续 tick 数 |
| `GoetyMech.slam_start_y` | `getSlamStartY/setSlamStartY` | 猛击起始 Y 坐标 |
| `GoetyMech.prev_crouch` | `getPrevCrouch/setPrevCrouch` | 上一 tick 的 crouch 状态（上升沿检测用） |
| `GoetyMech.slam_invuln_tick` | `getSlamInvulnTick/setSlamInvulnTick` | 猛击无敌帧剩余时间（猛击全程 + 落地后 20 tick = 1 秒） |
| `GoetyMech.wall_running` | `isWallRunning/setWallRunning` | 爬墙状态标志 |
| `GoetyMech.wall_nx/ny/nz` | `getWallNormal/setWallNormal` | 墙面法线方向 |
| `GoetyMech.wall_run_tick` | `getWallRunTick/setWallRunTick` | 爬墙已持续 tick 数 |
| `GoetyMech.jump_charge_tick` | `getJumpChargeTick/setJumpChargeTick` | 跳跃蓄力 tick 数 |
| `GoetyMech.sprint_tick` | `getSprintTick/setSprintTick` | 地面冲刺已持续 tick 数 |
| `GoetyMech.flight_forward` | `getFlightForward/setFlightForward` | 飞行 W 键输入状态（0 或 1） |

#### 装备检测（多处使用）

`isHoldingPrototype(entity)` — 仅检测 `EquipmentSlot.CHEST`
`hasPrototypeInCurios(player)` — 仅检测 Curios 任意槽位（try-catch 包裹防止 Curios 缺失 NPE）
`hasAnyPrototype(entity)` — 同时检测胸甲槽 + Curios 槽

### 2. 事件监听器（Forge 总线）

全部在 `PrototypeEvents` 中通过 `@SubscribeEvent` 注册（`Mod.EventBusSubscriber` + `Bus.FORGE`）：

| 事件 | Phase | 用途 |
|------|-------|------|
| `LivingFallEvent` | — | 持有原型或正在 slam 时取消坠落伤害 |
| `LivingAttackEvent` | — | `slam_invuln_tick > 0` 时免疫所有伤害 |
| `LivingEvent.LivingTickEvent` | — | slam 状态机（触发/下落）；slam invuln 递减；prevCrouch 记录 |
| `TickEvent.PlayerTickEvent` | START | 爬墙检测/状态更新、冲刺加速属性修饰器管理 |
| `TickEvent.PlayerTickEvent` | END | 爬墙运动学应用 `applyWallMovement` |
| `TickEvent.LevelTickEvent` | END | 环动画逐 tick 推进、延迟伤害到期触发 |
| `LevelEvent.Unload` | — | 清理 `ACTIVE_SLAMS` / `PENDING_DAMAGES` |

**关键时序**：slam 状态机在 `LivingTickEvent` 中处理（先于 `updateFallFlying`），可同 tick 停飞。爬墙运动学在 `PlayerTickEvent.END` 中应用（先于移动结算）。

### 3. 双阶段循环

```
飞行阶段                                   猛击阶段
┌──────────────┐    shift 按下             ┌─────────────────┐
│ Elytra Flight │ ────────────────────────→ │  Hover (SLAM_HOVER_TICKS) │
│ (无额外加速)   │                          │  水平×0.9减速，垂直悬停    │
│ 仅抗 drag    │                          │  slam_tick++              │
└──────────────┘                          └────────┬────────────┘
       ↑                                           │ hover 结束
       │                                           ↓
       │                                   ┌─────────────────┐
       │   jump 按下（取消）                │  Descend (加速下落) │
       │   ←───────────────────────────    │  V(t) = 0.15 + gravity×t │
       │                                   │  上限 SLAM_MAX_SPEED     │
       │                                   └────────┬────────────┘
       │                                            │ onGround() || inFluid()
       │                                            ↓
       │                                   ┌─────────────────┐
       │                                   │  Impact (冲击)    │
       │                                   │  ├ AoE 实体伤害   │
       │                                   │  ├ 方块环升起动画  │
       │                                   │  ├ 红色 AoE 圆形粒子│
       │                                   │  ├ 镜头震动       │
       │                                   │  ├ 爆炸音效       │
       │                                   │  ├ 发送 S2C 包   │
       │                                   │  ├ 设置 invuln=20 tick │
       │                                   │  └ 调度 60tick 后 │
       │                                   │    二次魔法伤害   │
       │                                   └─────────────────┘
```

#### 3.1 进入猛击

`handleSlamTrigger()`：检测 `nowCrouch && !prevCrouch`（shift 键上升沿）→ `enterSlamState()`：
- 设置 `slamming=true`、`slam_tick=0`、`slam_start_y=player.getY()`
- `elytraFlightTick()` 返回 false → 同 tick 停飞
- 设置初始下速度 `-0.15`
- **设置 invuln=9999**（猛击全程无敌）
- 播放火箭发射音效

#### 3.2 猛击下落

`handleSlamTick()`：
- `tick < SLAM_HOVER_TICKS`：水平 ×0.9 减速，垂直悬停（滞空）
- `tick >= SLAM_HOVER_TICKS`：`速度 = 0.15 + gravity_per_tick × (tick - hover_ticks)`，上限 SLAM_MAX_SPEED
- 强制关闭创造飞行

#### 3.3 冲击

`triggerSlamImpact()`：

```
fallDistance = max(0, slamStartY - player.getY())

damage = clamp(fallDistance × SLAM_DAMAGE_MULTIPLIER,
               SLAM_MIN_DAMAGE, SLAM_MAX_DAMAGE)

radius = clamp(fallDistance × SLAM_RADIUS_MULTIPLIER,
               8.0, SLAM_MAX_RADIUS)

rings  = min(4 + max(0, (fallDist-10)/20), 9, maxRingsByRadius)
       其中 maxRingsByRadius = max(0, floor(radius)-5)
```

- AoE 伤害：AABB `[x±radius, groundY-2 ~ groundY+3, z±radius]`，`DamageSource.playerAttack(player)`，均匀分布无衰减
- 每次命中：向外击飞（水平 2.5 + 垂直 0.5~0.8）
- **设置 slam_invuln_tick=20**（落地后 1 秒无敌，通过 `LivingAttackEvent` 实现）
- 立即清除 slam 状态
- 调度 `PENDING_DAMAGES`（60 tick 后二次伤害）

### 4. 二次延迟伤害（Delayed Damage）

| 属性 | 值 |
|------|-----|
| 触发时机 | 首次冲击后 60 ticks（3 秒） |
| 伤害量 | 首次伤害 × 50% |
| 伤害类型 | `indirectMagic(player, player)` — 无视护甲 |
| 特效 | 白色 `CircleExplodeParticleOption` 扩张环 + `END_PORTAL_SPAWN` 音效 |
| 击飞 | 水平 3.0 + 垂直 1.0~1.5 |

实现：`PENDING_DAMAGES: Map<ServerLevel, List<DelayedDamageData>>`，`onLevelTick` 每 tick 检查 `gameTime >= triggerTick`。

### 5. 环动画系统（Ring Animation）

基于 Goety `ModFallingBlock` 实体，逐圈扩散方块动画。

```
ACTIVE_SLAMS: Map<ServerLevel, List<SlamAnimData>>
```

**`SlamAnimData`**：`center` / `floorY` / `radius` / `totalRings` / `nextRingToSpawn` / `animTick` / `blocks` / `pendingBlocks` / `allRingsSpawned`

**每 tick 推进**（`onLevelTick` END）：
1. `animTick++`
2. 从 `pendingBlocks` 释放到期的延迟方块
3. 若 `animTick >= nextRingToSpawn × RINGS_DELAY(1)` → `spawnSingleRing()`
4. 移除已死亡的 `ModFallingBlock`
5. 所有 ring 生成完毕 + 无活跃/待处理方块 → 移除该 `SlamAnimData`

**环生成**（`spawnSingleRing`）：
- 环起始半径 = `5.0 + ringIdx`，宽度 1 格
- 通过 `findGroundPos` 找到地面方块（当前格 + 向下 3 格）
- **非最外环** → `POPUP_ANIM` 模式：`popUp = sqrt(desiredHeight × 0.2)`，随机延迟 0-7 tick 错开
- **最外环** → `MOBILE` 模式：向外飞散 + 旋转 + 重力

#### 同步视觉（`spawnVisuals`，冲击时触发）

| 视觉 | 实现 |
|------|------|
| 方块升起环 | `startRingAnimation`（逐圈错开） |
| MOBILE 飞散碎块 | 20 个 `ModFallingBlock(MOBILE)`，水平 3 格内随机 |
| 中心 DEBRIS | 15 个 `ModFallingBlock(MOBILE)`，0.5~2.0 格向外 |
| 红色外圈边界 | `CircleExplodeParticleOption(1,0,0, radius, 2)` — 即时爆发 |
| 红色 AoE 圆形 | `AoEParticleOption(radius, 60)` — 持续 3 秒半透明红圈 |
| 镜头震动 | `CameraShake.cameraShake(radius×1.5, 0.15, 15, 10)` |
| 爆炸音效 | 服务端 + 客户端（S2C 包） |

### 6. 爬墙系统（Wall-Running）

#### 6.1 墙面检测（`WallUtil.getWallDirection`）

取玩家两个 AABB 采样盒（脚部 30% + 头顶 15% 高度），向 ±X/±Z 各扩展 `range=0.5`，检测是否与方块碰撞：

```
脚部盒子: (x±width, y, z±width) → (x±width, y+height×0.3, z±width)
头顶盒子: (x±width, y+height×0.85, z±width) → (x±width, y+height, z±width)
```

返回法线向量 `Vec3(wallX, 0, wallZ)` 或 null。

#### 6.2 进入爬墙

`tryStartWallRun()`（PlayerTick START）：
- 条件：持有原型 + 非 slam + 非地面 + 非液体 + 有墙面
- 设置 `wall_running=true`、`wall_normal`、`wall_run_tick=0`
- 若正在飞行 → `stopFallFlying()`
- **继承速度**：分解为沿墙面分量 + 入墙面分量，去掉入墙分量保留切向速度

#### 6.3 维持爬墙

`handleWallRunTick()`（PlayerTick START）：
- 每 tick 检测：若 shift/液体/地面/飞行/墙壁消失 → `stopWallRun()`
- `wall_run_tick++`

#### 6.4 爬墙运动学

`applyWallMovement()`（PlayerTick END）：

```
surfaceVel = 当前速度投影到墙面切平面
forwardOnWall = 视线方向投影到墙面切平面（单位向量）
rightOnWall = forwardOnWall × wallNormal（单位向量）

inputAccel = forwardOnWall × zza × baseAccel
           + rightOnWall × strafe × baseAccel
           其中 baseAccel = MOVEMENT_SPEED × 0.3

surfaceVel = (surfaceVel + inputAccel) × WALL_FRICTION(0.98)
surfaceVel += (0, -WALL_SLIDE_SPEED(0.005), 0)  # 极轻微下滑

最终速度 = surfaceVel + wallNormal × WALL_ADHESION(0.05)
```

#### 6.5 客户端视觉效果

`PrototypeRenderEvents`：
- **Camera 60° roll**（`ViewportEvent.ComputeCameraAngles`）：根据墙面法线相对视角右方向，平滑过渡倾斜
- **Model 60° tilt**（`RenderPlayerEvent.Pre`）：玩家 body/head/arms/legs 同步 `zRot` 倾斜
- 平滑插值：`clientWallRoll += diff × 0.15`

### 7. 跳跃蓄力（Jump Charge）

#### 7.1 客户端检测

`onClientTick`（`PrototypeRenderEvents`）：
- `jumpDown && hasPrototype && !isFallFlying && (onGround || wallRunning)`
- 蓄力期间每 2 tick 发送 `C2SJumpChargePacket(tick)`（减少网络量）
- 粒子反馈：每 3 tick 生成 `CRIT` 粒子；超过 40 tick 额外生成 `ENCHANTED_HIT` 粒子
- jump 松开时发送 `C2SJumpChargePacket(0)` 触发跳跃

#### 7.2 服务端处理

`handleJumpChargeFromClient()`：
- `action > 0`：存储蓄力 tick（上限 `JUMP_CHARGE_MAX_TICKS`）
- `action == 0`：执行蓄力跳跃

#### 7.3 跳跃高度公式

```
threshold = JUMP_THRESHOLD_TICKS(20)
if charge < threshold:
    height = JUMP_SMALL_HEIGHT(3.0)
else:
    t = (charge - threshold) / (maxTicks - threshold)
    height = smallHeight + initialBonus + t × (maxHeight - smallHeight - initialBonus)

jumpPower = sqrt(2 × 0.08 × height)
```

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `jump_threshold_ticks` | 20 | 蓄力阈值，小于此值触发小跳 |
| `jump_small_height` | 3.0 | 小跳高度（格） |
| `jump_charge_initial_bonus` | 5.0 | 蓄力初始额外高度 |
| `jump_charge_max_height` | 30.0 | 蓄力满时最大高度 |
| `jump_charge_max_ticks` | 60 | 最长蓄力时间（3 秒） |

#### 7.4 Mixin 阻止普通跳跃

`PrototypePlayerMixin` → `Player.jumpFromGround()` HEAD 注入：
- `jumpChargeTick > 0` → cancel（防止蓄力时按空格触发跳跃）
- `jumping && 胸甲槽有原型` → cancel（防止飞行时误触）

### 8. 冲刺加速（Sprint Acceleration）

`onPlayerTick` START phase：
- 条件：持有原型 + `isSprinting()` + 地面 + `zza > 0` + 非 slam + 非 wall-run + 非飞行
- `sprint_tick++`，`level = min(sprint_tick / 20, 3)`
- 每 tick 替换 `MOVEMENT_SPEED` attribute：`multiply_total × (0.6 + level × 0.2)`
- sprint 停止时清除 modifier

### 9. 猛击无敌帧

实现两层保护：

| 保护 | 实现 | 持续时间 |
|------|------|---------|
| 猛击中 | `enterSlamState` 时设置 `invuln=9999` | 整个猛击过程 |
| 落地后 | `triggerSlamImpact` 时设置 `invuln=20` | 落地后 1 秒 |

通过 `LivingAttackEvent` 监听：`slam_invuln_tick > 0` → `setCanceled(true)`。

`LivingTickEvent` 中每 tick 递减 `invuln`（减到 0 后恢复受击）。

### 10. 飞行力学

飞行阶段使用 Hybrid 模型：**双注入点架构**，覆盖 W 按下（HEAD velocity-set）和 W 松开（TAIL 加法漂移）。全部实现在 `PrototypeTravelMixin` 中。

#### 10.1 V6 Hybrid 模型

每 tick 从客户端 `PrototypeRenderEvents.onClientTick` 同步 W 键状态到服务端：
```
客户端: 每 tick 写本端 + 每 3 tick 发 C2S → C2SFlightInputPacket(forward)
服务端: 存储至 entity PersistentData → TAG_FLIGHT_FORWARD
```

**双注入点架构**：

```
HEAD injection (cancellable=true):
  forward > 0.01 → velocity-set + pitch 依赖 lift + 俯冲 dot+gravity 投影
  forward ≤ 0.01 → 不拦截，fall through 到 vanilla travel()

TAIL injection:
  forward ≤ 0.01 → 速度 ×0.8，加法 baseAccel（水平视线方向）
  forward > 0.01 → 跳过（已由 HEAD 处理）
```

**HEAD 注入 — velocity-set + 俯冲 dot+gravity 投影**（`gp$onTravelHead`）：

当 W 按下时，根据俯仰角选择两套独立模型：

**水平/抬头（pitch ≤ 0.01）**：
```
targetVy = look.y × speed + liftFactor × hoverLift
liftFactor = clamp(-pitch / 90, 0, 1)    # 仰头越大 lift 越大

vMotion = (look.x × speed, targetVy, look.z × speed)

if newH < 0.001:          → 直视正上/下，水平归零
elif currH > newH:        → 保持旧水平动量大小
else:                     → 用 velocity-set 新值
```

**俯冲（pitch > 0.01）**：
```
speedAlongLook = max(speed, current·look + 0.08 × (-look.y))  # dot+gravity 投影
targetMotion = look × speedAlongLook

motion = current × 0.3 + targetMotion × 0.7    # 70/30 混合平滑过渡
```

**TAIL 注入 — 不按 W 的 0.8 倍速**（`gp$onTravelTail`）：

当 W 松开时，所有速度目标值取 W 状态的 80%：

```
noWSpeed = speed × 0.8
noWHoverLift = hoverLift × 0.8

if pitch > 0.01 (俯冲):
    speedAlongLook = max(noWSpeed, current·look + 0.08 × (-look.y))
    targetMotion = look × speedAlongLook
    velocity = current × 0.3 + targetMotion × 0.7
    水平: += lookH × baseAccel

else (水平/抬头):
    targetVy = look.y × noWSpeed + noWHoverLift × liftFactor
    velocity = current + lookH × baseAccel
    Y = targetVy
```

**效果**：

| 场景 | 机制 | 手感 |
|------|------|------|
| 按住 W 平飞 | velocity-set `targetSpeed=0.5` | 即时响应，稳定前飞 |
| 按住 W 抬头 | velocity-set + lift 随仰角线性增加 | 抬头上升不掉水平 |
| 按住 W 俯冲 | dot+gravity 投影 + 70/30 混合 | 速度沿视线方向对齐，水平不落后 |
| 松开 W | 全速率 ×0.8 + 水平 drift | 温和向前漂移，速度低于 W 状态 |
| 直视正上 | `newH < 0.001` → 水平归零 | 纯垂直上升，无水平偏移 |

**内部比率**（硬编码，不暴露为配置）：

| 值 | 计算 | 来源 |
|----|------|------|
| `hoverLift` | `speed × 0.16` | 兼顾 lift 与惯性保留 |
| `baseAccel` | `speed × 0.04` | 无 W 漂移加速度 |
| `noWSpeed` | `speed × 0.8` | 不按 W = 按 W 的 4/5 |
| `liftRatio` | `0.16` | hoverLift / speed |
| `accelRatio` | `0.04` | baseAccel / speed |

**配置项**（`GoetyMech-common.toml` → `prototype` 段）：

| 配置项 | 默认值 | 范围 | 说明 |
|--------|--------|------|------|
| `flight_target_speed` | 0.5 | 0.1~2.0 | **唯一速度配置**。驱动 lift(×0.16)、漂移(×0.04)、无W速度(×0.8) |

**代码位置**：
- `mixin/PrototypeTravelMixin.java` — HEAD + TAIL 双注入
- `client/PrototypeRenderEvents.java` — C2S 发送 + 本端 forward 写入
- `common/item/PrototypeItem.java` — getFlightForward / setFlightForward
- `config/ModConfig.java` — FLIGHT_TARGET_SPEED
- `common/net/c2s/C2SFlightInputPacket.java` — 网络传输

**与其他系统互斥**：
- Slam 期间 `isFallFlying() = false` → Mixin return
- 爬墙期间 `PlayerTickEvent.END` 覆写 velocity → Mixin 效果被覆盖（无害）

### 11. 取消猛击

```
客户端：TickEvent.ClientTickEvent.END
  └ 检测 keyJump 上升沿（wasPressed）→ 非爬墙时发送 C2SCancelSlamPacket

服务端：C2SCancelSlamPacket.handle()
  └ 校验 isSlamming() → 设置向上速度 max(0.5, currentY) → clearSlamState() → fallDistance=0
```

### 12. 客户端渲染

`PrototypeRenderEvents`：

| 事件 | 用途 |
|------|------|
| `RenderPlayerEvent.Post` (LOWEST) | 玩家 body 后渲染翅翼模型（`textures/entity/prototype.png`），同时检测 Curios 槽位 |
| `ViewportEvent.ComputeCameraAngles` | 爬墙时 camera roll 60° |
| `RenderPlayerEvent.Pre` | 爬墙时 player model 整体倾斜 |
| `TickEvent.ClientTickEvent.END` | 墙壁状态更新、取消 slam 发送、跳跃蓄力 C2S 发送 |
| `S2CSlamImpactPacket` handle | 客户端播放 `BlockParticleOption` 雾化粒子 + `GENERIC_EXPLODE` 音效 |

### 13. Curios 兼容

`PrototypeCurio` → `AttachCapabilitiesEvent<ItemStack>`：

| 方法 | 行为 |
|------|------|
| `curioTick` | 在 Curios 槽中继续飞行 tick（调用 `elytraFlightTick`） |
| `canEquip` | 无 Caelus 时禁止放入 Curios 槽；胸甲槽已有原型时禁止放入背/胸槽 |
| `canEquipFromUse` | 仅 Caelus 加载时可右键装备 |
| `getEquipSound` | 鞘翅装备音效 |

### 14. Caelus 兼容

`CaelusFlightHandler` → 当原型在 Curios 背部槽时，通过 `caelus:flight` attribute 添加飞行能力：
- `ADDITION` 修饰器，值 1.0
- 每 tick 清除后重新添加（防止作弊/命令修改）
- 通过 `ResourceLocation` 字符串查询 attribute，避免直接 import Caelus 类

### 15. 配置参数（`GoetyMech-common.toml`）

#### Prototype 段

| 配置项 | 默认值 | 范围 | 说明 |
|--------|--------|------|------|
| `slam_gravity_per_tick` | 0.25 | 0.01~2.0 | 每 tick 下落加速度 |
| `slam_max_speed` | 6.0 | 0.5~20.0 | 最大下落速度 |
| `slam_damage_multiplier` | 1.0 | 0.0~10.0 | 伤害 = 坠落高度 × 此值 |
| `slam_radius_multiplier` | 1.0 | 0.0~20.0 | 半径 = 坠落高度 × 此值 |
| `slam_min_damage` | 20.0 | 1~9999 | 最低伤害保底 |
| `slam_max_damage` | 1200.0 | 1~9999 | 伤害上限 |
| `slam_max_radius` | 100.0 | 1~200 | 半径上限 |
| `slam_hover_ticks` | 5 | 0~100 | 下落前悬停 tick 数 |
| `slam_ground_deform_radius` | 6.0 | 6.0~12.0 | 地面变形最大半径，同倍率增长 clamp[6, config] |
| `flight_target_speed` | 0.5 | 0.1~2.0 | 唯一速度配置（驱动 lift×0.16, drift×0.04, noW×0.8） |

#### Wall Run 段

| 配置项 | 默认值 | 范围 | 说明 |
|--------|--------|------|------|
| `wall_adhesion` | 0.05 | 0.01~0.5 | 每 tick 吸附拉向墙面力度 |
| `wall_friction` | 0.98 | 0.9~1.0 | 墙面速度衰减系数 |
| `wall_slide_speed` | 0.005 | 0.0~0.1 | 沿墙面下滑速度 |

#### Jump Charge 段

| 配置项 | 默认值 | 范围 | 说明 |
|--------|--------|------|------|
| `jump_threshold_ticks` | 20 | 5~100 | 蓄力阈值 |
| `jump_small_height` | 3.0 | 1.0~10.0 | 小跳高度 |
| `jump_charge_initial_bonus` | 5.0 | 1.0~30.0 | 蓄力初始额外高度 |
| `jump_charge_max_height` | 30.0 | 5.0~100.0 | 蓄力最大高度 |
| `jump_charge_max_ticks` | 60 | 20~200 | 最长蓄力时间 |

### 16. 关键设计决策

| 决策 | 原因 |
|------|------|
| 状态存 PersistentData 而非物品 NBT | 玩家切换 Curios 槽位后 ItemStack 改变，但状态应跟玩家走 |
| 环起始半径 3 格 | 避免冲击中心正下方方块被挖空，保留站脚位置 |
| `ConcurrentHashMap` + `synchronized` | ACTIVE_SLAMS/PENDING_DAMAGES 可能被多线程访问 |
| `indirectMagic` 二次伤害 | 无视护甲和附魔，与冲击的 `playerAttack` 形成互补 |
| `findGroundPos` 找地面方块 | 允许环动画在悬空洞穴中找到下方可用方块 |
| 二次伤害取消方块上升和红圈 | 减少视觉杂乱，只保留白色圆环 |
| 爬墙两层 AABB 采样 | 脚部和头顶各采一点，避免全 AABB 误判 |
| `try-catch` 包裹 Curios 调用 | 防止 Curios 未加载时 NPE 导致崩溃 |
| `flight_target_speed` 单配置驱动全部速率 | 未来修改者只需改一个值，lift/drift/noW 按内部比率自动缩放 |
| 俯冲使用 dot+gravity 投影而非 velocity-set | 保持速度沿视线方向，避免俯冲时水平/垂直不同步 |
| 不按 W = 按 W 的 80% | 飞行时有区分感，但漂移不弱到难以控制 |

## 调试日志

所有关键事件输出至 `GoetyMechMod.LOGGER`（INFO 级别），通过 `run/logs/latest.log` 可追踪：
- 猛击触发/取消、冲击伤害/半径/环数
- AoE 命中/跳过实体及距离
- 环动画的每个方块位置和 popUp 值
- 爬墙开始/停止、墙面法线
- 跳跃蓄力/释放/高度
- Mixed 取消普通跳跃（`jumpChargeTick > 0`）

`PlayerTickEvent.START` 还输出 `zza` / `onGround` / `isFallFlying`（DEBUG 级别），用于调试飞行状态检测。
