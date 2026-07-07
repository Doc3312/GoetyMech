# Wall-Run 系列调试经验汇总

status: consolidated
source: 合并自 4 个 incident 文件
date: 2026-06-23 ~ 2026-06-24

---

## 墙跑无实际移动

来源: _meta.20260623.wall-run-no-movement.md

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

---

## 墙跑无实际移动 — V2（player.move() 不产生位移）

来源: _meta.20260623.wall-run-no-movement-v2.md

# 墙跑无实际移动 — V2（player.move() 不产生位移）

status: superseded

## 2026-06-23 Post-archive update
status: superseded

Client-side setDeltaMovement approach was implemented but missed the design goal.
The user wants VERTICAL wall-running (run UP walls, Titanfall-style), not horizontal wall-running.
W=climb up, S=slide down, A/D=strafe left/right on wall surface.
Gravity should pull TOWARD the wall (not projected onto wall plane).
No auto-forward movement - player controls all direction.

## 问题

墙跑完全实现（相机全量旋转、转角检测、摩擦检测）后，服务端 `player.move(MoverType.SELF, move)` 不产生实际位移。Y 坐标始终停留在地面高度，沿墙方向也无移动。

## 已确认工作的功能

- 相机全量旋转（客户端视觉）：`Quaternionf.rotationZ()` + SLERP，按下 Ctrl 靠近墙壁时视角正确旋转
- 墙跑激活与持续性：`WALL_MV` 日志每 tick 输出，`isWallRunning` 始终为 true
- 移动向量计算正确：`move=(-0.3000, -0.0300, -0.6000)`（墙在左，沿墙 Z=-0.6，向墙 X=-0.3，慢滑 Y=-0.03）
- 地面起飞 lift（`vy=0.42`）：`MIXIN_AISTEP_RETURN` 显示 `onGround=false`
- 转角自动检测：当前墙消失时 `findBestWallDirection` 扫描 4 方向并切换 normal
- 摩擦检测：`getWallFriction >= 0.8` 阻止/停止跑墙

## 关键证据

同一 tick 的日志顺序：

```
D START: zza=0.000, onGround=true, isFly=false, hSpeed=0.0000
WALL_DIAG: isSprint=true, zza=0.000, xxa=0.000, curVel=(0.0000, -0.0784, 0.0000)
WALL_MV: wallN=(1.00, 0.00, 0.00), fwd=1.0, strafe=0.0, fwOnWall=(0.000, 0.000, -1.000), right=(0.000, 0.000, 0.000)
WALL_MV_RESULT: move=(-0.3000, -0.0300, -0.6000), gravityOnWall=(0.0000, -0.0300, 0.0000), pull=(-0.3000, 0.0000, 0.0000)
MIXIN_AISTEP_RETURN: zza=0.000, jumping=false, onGround=false, sprint=true
D END: onGround=false, isFly=false, hSpeed=0.0000, sprintTick=0, zza=0.000
```

矛盾点：
1. `D START` 说 `onGround=true` 但 `curVel.y=-0.0784`（在地面有重力速度？）
2. `D END` 说 `onGround=false` 但 `hSpeed=0.0000`（lift 生效了，但水平移动量为 0）
3. `WALL_MV_RESULT` 说 `move=-0.6` 沿墙（Z），但 `D END` 显示无水平速度

## 诊断历程

### 已排除的假设

| # | 假设 | 排除方式 | 结论 |
|---|------|----------|------|
| 1 | `setDeltaMovement` 被 travel 摩擦覆盖 | 改用 `player.move() + setDeltaMovement(ZERO)`，绕过 travel | 排除 |
| 2 | 前进输入未送达 | `forward=1.0` 默认 fallback，日志确认 fwd=1.0 | 排除 |
| 3 | 移动方向错误 | 改为 `down × wallN` 水平沿墙方向，日志确认方向正确 | 排除 |
| 4 | sprint 状态丢失 | `isSprint=true` 日志确认 | 排除 |
| 5 | move 向量太小 | WALL_RUN_SPEED=0.6，move 沿墙分量 0.6 m/tick | 排除 |
| 6 | onGround lift 无效 | `MIXIN_AISTEP_RETURN` 确认 `onGround=false` | 排除 |

## 开放问题

**为什么 `player.move(MoverType.SELF, move)` 不改变玩家位置？**

`Entity.move()` 是 vanilla 中执行碰撞位移的标准方法。它从当前 position 沿 move 向量做 AABB sweep，检测碰撞后设置新 position。日志确认 `move` 向量正确（`(-0.3, -0.03, -0.6)`），`move()` 方法被调用，但玩家不移动。

**猜想 A：服务端 tick 中 `player.move()` 被某些机制阻止**
- 可能原因：`ServerPlayerGameMode` 的移动限制、碰撞检测返回静态碰撞（把玩家"挤回"原位）、某些 mod 或 mixin 拦截了 move()
- 检测方案：在 `player.move()` 前后各取一次 `player.position()` 对比，判断 move 是否生效

**猜想 B：时序问题 — 墙跑在 applyWallMovement 执行前已被停止**
- D START 的 `onGround=true` + `curVel.y=-0.0784` 暗示墙跑可能未正确维持
- 如果 `handleWallRunTick` 在 START phase 停止了墙跑，那么 END phase 的 `applyWallMovement` 不会执行；但日志确认 WALL_MV 输出，说明它执行了
- 更可能是：墙跑在每个 tick 被正确维持和移动，但 move 的累计效果被下个 tick 的 vanilla 物理（`travel()`）"重置"了

**猜想 C：`player.move()` 位移后被下个 tick 的 `travel()` 逆操作**
- `travel()` 不仅处理 velocity→move，还处理 friction、gravity、collision
- 如果 `travel()` 中的 `moveRelative()` 生成了一个相反的 move，可能抵消墙跑的位移
- 但 wall-run 时 `setDeltaMovement(ZERO)` 被设置，travel() 读到的 velocity 为 0，不应产生反方向位移

**猜想 D：Minecraft 的玩家移动有最小位移阈值**
- 检查 `Entity.move()` 内部：是否有 `move.length() < threshold` 就跳过的逻辑
- 但 0.6 m/tick 远大于任何合理阈值

## 涉及文件

- `PrototypeEvents.java:419-471` — `applyWallMovement()` 完整实现
- `PrototypeEvents.java:345-358` — `tryStartWallRun()` 激活
- `PrototypeEvents.java:360-400` — `handleWallRunTick()` 维持
- `PrototypeRenderEvents.java` — 客户端相机旋转（working）
- `ModConfig.java` — 墙跑参数配置
- `openspec/changes/wall-run-full-rotation/` — 完整 openspec change（已归档）

## 时间线

| 时间 | 事件 |
|------|------|
| 2026-06-23 18:57 | openspec change 创建（proposal + design + 3 specs + 21 tasks） |
| 2026-06-23 18:58 | 相机旋转实现：四元数 + SLERP 平滑过渡 |
| 2026-06-23 19:00 | applyWallMovement 实现：gravityOnWall + pull + move |
| 2026-06-23 19:05 | 所有 21 个 tasks 标记完成 |
| 2026-06-23 19:30 | 调试发现 move() 执行但不产生位移 |
| 2026-06-23 20:00 | 替换为 `player.move() + setDeltaMovement(ZERO)` 模型 |
| 2026-06-23 20:15 | 添加 WALL_DIAG / WALL_MV / WALL_MV_RESULT 日志 |
| 2026-06-23 21:00 | 确认方向/速度/激活全部正确，但无位移 → 归档 |

---

## 墙跑屋檐跨越方案（成功）

来源: _meta.20260623.wall-run-overhang-vault.md

# 墙跑屋檐跨越方案（成功）

status: verified

## 问题

I 形墙顶部加一排向外延伸方块（倒 L 形屋檐），玩家墙跑攀爬到顶时被卡住，无法越过。

## 失败尝试

1. **后处理 teleport**（Y 抬升 + wallRight 平移）→ 视觉突兀
2. **预判 sidestep**（wallRight 方向绕行）→ 屋檐横跨整面墙，绕不开
3. **向外推开 vault**（wallN.scale(-out) + climb）→ 推开清除了屋檐，但玩家飞出检测范围（0.5），下一 tick 失去墙面吸附，坠落

## 成功方案

**向外推开（vault）+ 宽限期（grace period）**

两层机制：

### 1. 预判向外推开（`MixinWallRunTravel.java:48-56`）

```java
for (double out : new double[]{0.9, 1.2, 0.7, 0.5}) {
    Vec3 outward = wallN.scale(-out);
    AABB checkBox = player.getBoundingBox().move(climbVec.add(outward)).inflate(-0.02);
    if (player.level().noCollision(checkBox)) {
        wallMove = wallMove.add(outward);
        ClientWallRunState.applyGrace(3);  // ← 关键
        break;
    }
}
```

原理：`Entity.move()` 处理轴序为 X→Y→Z。向外推开在 X/Z 轴先执行，玩家离开墙面后 Y 轴碰撞检测在新位置进行，屋檐不再阻挡。

### 2. 宽限期吸附恢复（`ClientWallRunState.java` + `PrototypeRenderEvents.java`）

推开后玩家距墙面 ~0.9-1.2，超出检测范围 0.5。宽限期机制：

- `applyGrace(3)`：记录当前 wallNormal，设 3 tick 宽限
- `tickGrace()`：每 tick 递减，宽限期内 mixin 继续用 lastWallNormal
- 每 tick 有 0.3 的 pull 力拉向墙面
- 3 tick 后玩家回到 ~0-0.3 距离，检测恢复正常

## ParCool 对比

ParCool 不处理屋檐跨越。其 `VerticalWallRun.canContinue()` 检测 `playerYSpeed > 0`，头撞天花板后 Y 速度归零 → 墙跑自然结束。无 vault/climb-up 自动衔接。

## 关键决策

- **Entity.move() 轴序**：X→Y→Z 的顺序使推开后的 Y 检测在正确位置进行
- **宽限期而非增大检测范围**：避免误吸附远处墙面
- **3 tick 足够**：因 pull=0.3/tick，从 0.9 回到 0.3 需 2 tick，3 tick 留余量

---

## 翻越后 sprint 保持

来源: _meta.20260624.wall-run-sprint-retain.md

# 翻越后 sprint 保持

status: verified

## 问题

翻越头顶一格障碍（overhang vault）后，冲刺状态自动退出。墙跑的 sprint 续存逻辑无法覆盖翻越后的空白窗口。

## 根因

`LocalPlayer.aiStep()` 在 `travel()` 返回后会清除冲刺状态。我们的 travel mixin 虽然每 tick 设 `player.setSprinting(true)`，但翻越爆发后玩家短暂离开墙面，grace period 结束后 `wallNormal` 被清除，mixin 不再运行 → sprint 丢失。Toggle-sprint 模式下 key 不在按下状态，加重问题。

## 解决方案

在 `ClientWallRunState` 新增 `sprintRetainTicks` 计数器和配套 `shouldSprint()` / `tickSprintRetain()` 方法。翻越爆发时（MixinWallRunTravel.java 的 outward burst 成功分支）设 `sprintRetainTicks = 30`。

START 阶段用 `shouldSprint()` 守卫 sprintDown 覆写，并在 `sprinting` 变量中加 `|| shouldSprint()`。这样无论 wallNormal/grace 状态如何，翻越后 30 tick（1.5 秒）内 sprint 始终保持，足够拉动重新贴墙。

## 改动文件

- `ClientWallRunState.java`: +2 个方法 +1 字段
- `MixinWallRunTravel.java`: outward burst 成功时设 `sprintRetainTicks = 30`
- `PrototypeRenderEvents.java`: START 阶段守卫 + sprinting 变量补充

## 时间线

- 2026-06-24: 初始 10 tick 不够 → 用户手动改为 30 tick 验证通过

---
