# PrototypePlayerMixin — GoetyMech

## 身份

- 类型：mixin
- Java 类：PrototypePlayerMixin (mixin `Player`)
- 首次创建：2026-06-14

## 设计意图

Player.aiStep() 的诊断 Mixin，用于调试 Prototype 飞行/跳跃系统。
包含 `aiStep` HEAD/RETURN 日志注入，以及蓄力时取消跳跃的逻辑。

## Timeline

- **2026-06-27** — 降级诊断日志从 INFO 为 DEBUG。
