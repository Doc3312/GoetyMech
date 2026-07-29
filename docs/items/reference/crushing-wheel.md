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
- **2026-07-09** — 模型原点从 `[0,16,8]` 修正为 `[0,16,0]`
  - 改动：JSON 所有 element 的 Z 坐标 -8，旋转原点改为 Z=0；renderer 去掉 0.5 硬编码偏移；`makeBoundingBox` 去掉 `cz + 0.5`
  - 原因：消除轮子视觉偏移（有时偏左/偏右）
