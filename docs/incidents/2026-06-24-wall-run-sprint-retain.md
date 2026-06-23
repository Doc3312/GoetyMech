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
