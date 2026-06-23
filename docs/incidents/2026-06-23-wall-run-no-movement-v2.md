# 墙跑无实际移动 — V2（player.move() 不产生位移）

status: superseded

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
