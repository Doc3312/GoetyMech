---
name: "prototype-charge-jump"
description: "charge jump / jump height"
---

# Prototype 蓄力跳跃 — 开发 & 调参指南

> 前置模组源码路径见 `.opencode/skills/_shared/libcode-paths.md`。
> 快速排除：当前任务如果不涉及蓄力-释放跳跃、跳跃高度公式调参或落地检测，请跳过本 skill。

## 适用场景

- 实现蓄力-释放式（charge/release）跳跃机制
- 公式计算的跳跃高度与实际游戏内高度不符
- 需要精确测量实体跳跃高度（落地检测）
- 为跳跃系统添加 Config 可调参数
- 理解 Minecraft 垂直运动物理（gravity / drag 时序）
- 调试 Curios 槽位检测 + Mixin 跳跃压制

## 文档索引

| 文件 | 优先级 | 内容 |
|------|--------|------|
| [DEBUGGING.md](DEBUGGING.md) | ★ 最高 | 多次 build-run 调试经验、peakY 追踪法、WAS_AIRBORNE 状态机、实测数据（50→50.7, 80→60） |
| [PHYSICS.md](PHYSICS.md) | ★ 高 | 含 drag 精确速度公式推导、sqrt(2gh) 失效原因、velocityForHeight 二分法实现 |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 中 | 系统架构：release-based 设计、NBT 蓄力 tick、网络包流、START phase 时序、地面起飞覆盖陷阱 |

## 关键发现速查

| 问题 | 结论 |
|------|------|
| `sqrt(2 * g * h)` 为什么不准 | Minecraft 垂直有 0.98/tick drag，公式假设真空 |
| 落地高度为什么测得 0 | `onGround()` 在 START phase 是上 tick 状态，且平面起落 Y 相同 |
| 正确的高度测量方式 | 追踪飞行中最大 Y（peakY），不是起落 Y 差 |
| 80 理论值为何只跳 ~60 | 当前二分法公式在高速段仍有偏差（待调） |
| 蓄力跳没生效 | 检查地面起飞代码 (`zza>0 && charge==0` 会覆盖为 2.0） |
| Curios 原型检测不到 | 必须同时查 chest + Curios 槽（`hasAnyPrototype`） |
| 改配置没生效 | 需要删除 `run/config/GoetyMech-common.toml` 强制再生 |
