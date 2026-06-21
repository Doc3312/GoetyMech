# 风暴之翼 (Storm Wing) 架构说明

## 概述

风暴之翼是基于 Goety 模组、通过 Curios API 装备在胸甲槽位的鞘翅类物品。核心玩法为 **飞行 → 猛击** 双阶段循环，附带逐圈扩散的方块动画和二次延迟魔法伤害。

## 文件结构

```
src/main/java/com/goetypunk/addon/
├── common/
│   ├── item/
│   │   └── StormWingItem.java       # 物品定义 + PersistentData tag 访问器
│   ├── events/
│   │   └── StormWingEvents.java     # 核心逻辑：飞行、猛击状态机、环动画、伤害
│   └── net/
│       ├── ModNetwork.java          # 网络通道注册
│       ├── c2s/
│       │   └── C2SCancelSlamPacket.java  # 客户端→服务端：取消猛击
│       └── s2c/
│           └── S2CSlamImpactPacket.java  # 服务端→客户端：猛击冲击粒子+音效
├── client/
│   └── StormWingRenderEvents.java   # 客户端渲染 + 取消猛击按键检测
└── config/
    └── ModConfig.java               # 所有配置项（含 Storm Wing 参数）
```

## 核心架构

### 1. 物品生命周期

```
StormWingItem (ElytraItem 子类)
├── canElytraFly() = true
├── elytraFlightTick() → 非 slam 状态时返回 true（允许飞行），slam 时返回 false（强制停飞）
└── NBT Tag 访问器（静态方法）
    ├── isHoldingStormWing(entity)     — 检测胸部/Curios 槽位
    ├── isSlamming / setSlamming       — TAG_SLAMMING
    ├── getSlamTick / setSlamTick      — TAG_SLAM_TICK
    ├── getSlamStartY / setSlamStartY  — TAG_SLAM_START_Y
    ├── getPrevCrouch / setPrevCrouch  — TAG_PREV_CROUCH
    └── getFlightTick / setFlightTick  — TAG_FLIGHT_TICK
```

**所有状态存于实体的 PersistentData**，而非物品 NBT。原因：
- 玩家可切换 Curios 槽位（不同槽位是不同 ItemStack）
- 状态绑定玩家而非物品实例

### 2. 事件监听器（Forge 总线）

所有事件在 `StormWingEvents` 中通过 `@SubscribeEvent` 注册：

| 事件 | 时机 | 用途 |
|------|------|------|
| `LivingFallEvent` | 玩家即将受到坠落伤害 | 若正在 slam 则取消坠落伤害 |
| `LivingEvent.LivingTickEvent` | 每 tick（实体 tick 早期） | slam 状态机运转（触发、下落） |
| `TickEvent.PlayerTickEvent.START` | 每 tick（玩家 tick 早期） | 地面起飞检测 |
| `TickEvent.PlayerTickEvent.END` | 每 tick（玩家 tick 晚期） | 飞行加速度 + burst 检测 |
| `TickEvent.LevelTickEvent.END` | 每 tick（世界 tick 晚期） | 环动画推进 + 延迟伤害触发 |
| `LevelEvent.Unload` | 世界卸载 | 清理 ACTIVE_SLAMS / PENDING_DAMAGES |

**注意**：slam 状态机在 `LivingTickEvent` 中处理而非 `PlayerTickEvent`，因为前者在 `updateFallFlying` 之前触发，可在同一 tick 内停止飞行。

### 3. 双阶段循环

```
飞行阶段                                  猛击阶段
┌──────────┐    shift(蹲下)    ┌──────────────────┐
│  Elytra  │ ────────────────→ │  Hover (SLAM_HOVER_TICKS) │
│  Flight  │                   │  水平减速，垂直悬停        │
│  (加速/  │                   │  持续 tick++              │
│   转向)  │                   └────────┬─────────────────┘
└──────────┘                            │ hover 结束后
       ↑                                ↓
       │                        ┌──────────────────┐
       │   空格(取消)            │  Descend (加速下落) │
       │   ←──────────────────  │  速度 = 0.15 + gravity × tick │
       │                        │  上限 SLAM_MAX_SPEED         │
       │                        └────────┬─────────────────┘
       │                                 │  onGround() || inFluid()
       │                                 ↓
       │                        ┌──────────────────┐
       │                        │  Impact (冲击)     │
       │                        │  ├ AoE 实体伤害    │
       │                        │  ├ 方块环动画      │
       │                        │  ├ 红色 AoE 圆形粒子│
       │                        │  ├ 镜头震动        │
       │                        │  ├ 爆炸音效        │
       │                        │  ├ 发送 S2C 包     │
       │                        │  └ 调度 60tick 后  │
       │                        │    二次魔法伤害     │
       │                        └──────────────────┘
```

### 4. 猛击伤害公式

```
fallDistance = max(0, slamStartY - player.getY())

damage = clamp(fallDistance × SLAM_DAMAGE_MULTIPLIER,
               SLAM_MIN_DAMAGE,
               SLAM_MAX_DAMAGE)

radius = clamp(fallDistance × SLAM_RADIUS_MULTIPLIER,
               8.0,
               SLAM_MAX_RADIUS)
```

**关键设计**：伤害和半径在圆内**均匀分布**，无边缘衰减。

### 5. 二次延迟伤害（Delayed Damage）

- **触发时机**：首次冲击后 60 ticks（3 秒），对应 AoE 红色圆形消失时
- **伤害类型**：`indirectMagic(player, player)` → 无视护甲的魔法伤害
- **伤害量**：首次伤害 × 50%
- **特效**：白色 `CircleExplodeParticleOption` 扩张环 + `SoundEvents.END_PORTAL_SPAWN` 音效
- **击飞**：向外 3.0 格/tick 水平速度 + 1.0~1.5 垂直速度
- **实现**：`PENDING_DAMAGES` Map 存储到期的伤害数据，`onLevelTick` 每 tick 检查

### 6. 环动画系统（Ring Animation）

基于 `ModFallingBlock`（Goety 实体）的逐圈扩散方块动画。

```
ACTIVE_SLAMS: Map<ServerLevel, List<SlamAnimData>>
```

**SlamAnimData**：
- `center`：冲击中心
- `totalRings`：环数（默认 4-9，随坠落距离增加）
- `nextRingToSpawn`：当前已生成的环索引
- `blocks`：活跃中的 `BlockAnimData` 列表
- `pendingBlocks`：待延迟生成的方块

**每 tick 推进**（`onLevelTick`）：
1. 检查是否需要生成新环（`animTick >= nextRingToSpawn × RINGS_DELAY`）
2. 从 `pendingBlocks` 中释放到期的延迟方块
3. 移除已死亡的方块
4. 所有环生成完毕且方块清空后，移除该动画

**环生成**（`spawnSingleRing`）：
- 环半径 = `5.0 + ringIndex`，宽度 1 格
- 每个环遍历 AABB 内所有方块位置
- 非最外环 → `POPUP_ANIM` 模式（原地弹起动画，延迟 0-7 tick 错开）
- 最外环 → `MOBILE` 模式（向外飞散，带旋转和重力）

### 7. 取消猛击（Cancel Slam）

```
客户端：TickEvent.ClientTickEvent.END
  └ 检测 keyJump 上升沿（按下瞬间）
    └ 发送 C2SCancelSlamPacket → 服务端

服务端：C2SCancelSlamPacket.handle()
  └ 校验 isSlamming()
    └ 设置向上速度（max(0.5, currentY)）
    └ 调用 clearSlamState()：移除 slam 标记和 tick 数据
    └ fallDistance = 0（防止触发坠落伤害）
```

**为什么不在客户端校验 isSlamming？**

`onLivingTick` 首行 `if (player.level().isClientSide) return;` 导致客户端从不执行 `enterSlamState`，因此 `TAG_SLAMMING` 只存在于服务端 NBT。客户端 `isSlamming()` 恒返回 false。

解决方案：**客户端不校验**，直接发送包，服务端 handler 进行真正的状态校验。

### 8. 飞行力学

在 `onPlayerTick`（Phase.END）中实现：

```
# 抗 drag 补偿（抵消鞘翅默认 0.98 减速）
每 tick 将水平速度除以 0.98

# 加速（玩家按住 W 时）
沿视线方向增加 BOOST_ACCELERATION

# Burst（首次进入飞行）
横向 + 视线方向 × 1.25
```

**地面起飞逻辑**（Phase.START/END）：
- 手持风暴之翼 + 未飞行 + 在地面 + 按住 W → 设置垂直速度 2.0
- 手持风暴之翼 + 未飞行 + 在空中 + 按住 W → 强制 `startFallFlying()`

### 9. 客户端渲染

`StormWingRenderEvents`：

- **翅翼模型渲染**（`RenderPlayerEvent.Post`）：在玩家 body 位置后渲染风暴之翼纹理（`textures/entity/storm_wing.png`），附着于胸甲槽位。支持 Curios 槽位检测。
- **冲击特效**（`S2CSlamImpactPacket` 客户端 handler）：撞击位置的 `BlockParticleOption` 雾化粒子 + `GENERIC_EXPLODE` 音效

### 10. 配置参数（`goetypunk-common.toml`）

| 配置项 | 默认值 | 范围 | 说明 |
|--------|--------|------|------|
| `boost_acceleration` | 0.2 | 0.0~1.0 | 飞行每 tick 加速度 |
| `slam_damage_multiplier` | 1.0 | 0.0~10.0 | 伤害倍率：坠落高度 × 此值 |
| `slam_radius_multiplier` | 1.0 | 0.0~20.0 | 半径倍率 |
| `slam_min_damage` | 20.0 | 1.0~9999.0 | 最低伤害保底 |
| `slam_max_damage` | 1200.0 | 1.0~9999.0 | 伤害上限 |
| `slam_max_radius` | 100.0 | 1.0~200.0 | 半径上限 |
| `slam_gravity_per_tick` | 0.25 | 0.01~2.0 | 每 tick 下降加速度 |
| `slam_max_speed` | 6.0 | 0.5~20.0 | 最大下降速度 |
| `slam_hover_ticks` | 5 | 0~100 | 下落前悬停 tick 数 |

### 11. 关键设计决策

| 决策 | 原因 |
|------|------|
| 状态存 PersistentData 而非物品 NBT | 玩家切换 Curios 槽位后 ItemStack 改变，但状态应跟玩家走 |
| 环起始半径 5 格 | 避免冲击中心正下方方块被挖空，保留站脚位置 |
| PENDING_DAMAGES 用 ConcurrentHashMap | 多线程环境安全；服务端 LevelTick 可能来自不同线程 |
| 红圈使用 Goety 的 AoEParticleOption | 该粒子在客户端渲染为半透明圆盘，视野外隐藏 |
| 延迟伤害用 indirectMagic | 无视护甲和附魔，玩家无法减免，与冲击的 playerAttack 形成互补 |
| 通过 findGroundPos 找地面方块 | 允许环动画在悬空洞穴中仍能找到下方可用方块，而非凭空生成 |
| 二次伤害取消方块上升和淡紫色范围指示 | 减少视觉杂乱，只保留圆环特效作为伤害提示 |

## 调试日志

所有关键事件均输出 INFO 级别日志（`GoetyPunkMod.LOGGER`），包括：
- 飞行状态变化（prev/cur flight ticks）
- 猛击触发和取消
- Impact 时的伤害/半径/环数计算
- 每次 AoE 命中/跳过的实体及其距离
- 环动画的每个方块位置和 popUp 值

通过 `run/logs/latest.log` 可追踪完整逻辑。
