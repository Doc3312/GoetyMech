# Prototype Timeline

## 2026-07-13 — 受击免疫移至 LivingAttackEvent

- **改动**: 低伤害阈值免疫检查从 `LivingHurtEvent` 移至 `LivingAttackEvent`
- **原因**: `LivingHurtEvent` 取消时受击效果已发生（hurtTime/击退/音效/红屏），`LivingAttackEvent` 取消则整套 hurt() 管道短路，完全无受击效果——对齐 Goety 壁垒聚晶行为
- **关联**: `PrototypeEvents.java:188-202`, commit `34612fa`
