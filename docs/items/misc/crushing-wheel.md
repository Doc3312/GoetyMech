# 碾压轮 — GoetyMech

## 身份

- 类型：entity
- 注册类：ModEntities.CRUSHING_WHEEL
- Java 类：CrushingWheelEntity (MobCategory.MISC, 2.0x2.0)
- 首次创建：2026-06-14

## 设计意图

StressFocus 召唤的大型碾压轮实体，对路径上的生物造成碾压伤害。将 Create 的 Crushing Wheel 视觉转化为战斗投射物。

## 演化记录

- **2026-06-27** — 降级 CrushingWheelEntity 每 tick `[WheelTest]` 和粉碎事件日志从 INFO 为 DEBUG。
