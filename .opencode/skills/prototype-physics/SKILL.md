---
name: prototype-physics
description: "Prototype 物理系统（蓄力跳跃 + 飞行 Mixin）— 合并在原 charge-jump 与 flight-mixin 的所有调试经验、物理公式和架构记录"
superseded_by: "docs/incidents/_craft/"
---

# Prototype 物理系统 — 开发经验合并

> 本 skill 合并了原 `prototype-charge-jump` 和 `prototype-flight-mixin` 的全部内容。
> **状态：已归档，不活跃开发。** 物理原型阶段的完整经验记录。

## 文档索引

| 文件 | 行数 | 内容 |
|------|------|------|
| [charge-jump-skill.md](charge-jump-skill.md) | 39 | 蓄力跳跃入口指南 + 关键发现速查 |
| [charge-jump-debugging.md](charge-jump-debugging.md) | 102 | 调试经验 — peakY 追踪法、WAS_AIRBORNE 状态机、实测数据 |
| [charge-jump-physics.md](charge-jump-physics.md) | 113 | 含 drag 精确速度公式推导、sqrt(2gh) 失效原因、二分法实现 |
| [charge-jump-architecture.md](charge-jump-architecture.md) | 124 | 系统架构：release-based 设计、NBT 蓄力 tick、网络包流 |
| [flight-mixin-skill.md](flight-mixin-skill.md) | 215 | 飞行 Mixin 完整探索日志（V4/V5/V5b）+ Mixin 注入点分析 + 同步陷阱 |

## 经验快照

| 问题 | 结论 | 详见 |
|------|------|------|
| sqrt(2gh) 为什么不准 | 垂直有 0.98/tick drag | charge-jump-physics.md |
| 高度怎么测 | peakY 而非起落 Y 差 | charge-jump-debugging.md |
| 长蓄力丢失水平惯性 | 多源取 max + NBT cj_start_speed | charge-jump-architecture.md |
| 飞行加法模型无效 | 必须用 velocity-set（每 tick 重置） | flight-mixin-skill.md |
| client 端 forward 永远 0 | C2S handler 只跑服务端，发端必须同时写本端 | flight-mixin-skill.md |
| zza 只在 LocalPlayer 有效 | ServerPlayer 的 zza 永远为 0 | flight-mixin-skill.md |
