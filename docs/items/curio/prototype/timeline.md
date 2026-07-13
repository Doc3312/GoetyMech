# Prototype Timeline

## 2026-07-13 — 受击免疫移至 LivingAttackEvent

- **改动**: 低伤害阈值免疫检查从 `LivingHurtEvent` 移至 `LivingAttackEvent`
- **原因**: `LivingHurtEvent` 取消时受击效果已发生（hurtTime/击退/音效/红屏），`LivingAttackEvent` 取消则整套 hurt() 管道短路，完全无受击效果——对齐 Goety 壁垒聚晶行为
- **关联**: `PrototypeEvents.java:188-202`, commit `34612fa`

## 2026-07-13 — 地面变形半径可配置 + 阶梯增长 + 由内到外恢复

- **改动**: 地面变形半径改为可配置（`slam_ground_deform_radius`, 6~16），阶梯增长公式 `6 + floor(fallDist/20)`；zone 边界从硬编码改为等比划分；动画恢复从由外到内改为由内到外；config 默认值改为 16
- **原因**: 环动画每 20 格 +1，地面变形参考同模式；由内到外恢复视觉效果更自然
- **关联**: `ModConfig.java` `SlamGroundTiltHandler.java` `S2CSlamImpactPacket.java` `PrototypeEvents.java`, commit `c459622` `18e867e`
- **关联 incidents**: `_meta/20260713.config-cache-pitfall.md`
