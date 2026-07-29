# MixinWallRunTravel — GoetyMech

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
| 2026-06-24 | 头顶翻越（outward burst）+ grace period 3 tick | 墙跑遇到天花板障碍自动向外爆发翻越 | ClientWallRunState |
| 2026-06-24 | 翻越爆发时设 sprintRetainTicks=30（后改为 30） | 翻越后 sprint 被 LocalPlayer.aiStep() 清除，需要保留窗口重新贴墙 | ClientWallRunState, PrototypeRenderEvents |
