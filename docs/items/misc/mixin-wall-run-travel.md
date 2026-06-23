# MixinWallRunTravel — goetypunk

## 身份

- 类型：misc (Mixin)
- Java 类：MixinWallRunTravel
- Mixin 目标：`Player.travel(Vec3)`
- 首次记录：2026-06-24

## 设计意图

取代 `Player.travel()` 的默认移动逻辑，当玩家穿着 Prototype 且在爬墙状态时，使用自定义墙面运动向量（沿墙上爬+侧移+重力吸附），跳过原版移动计算。

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 首次文档记录 | 还清文档债务 | ClientWallRunState, PrototypeItem, WallUtil |
