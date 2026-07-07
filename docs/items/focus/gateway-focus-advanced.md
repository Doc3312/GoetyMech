# 神行聚晶 — GoetyMech

## 身份

- 类型：focus (advanced)
- 注册类：ModItems.GATEWAY_FOCUS_ADVANCED
- Java 类：GatewayAdvancedFocus (extends MagicFocus)
- 注册 ID：`goetymech:gateway_focus_advanced`
- 底层法术：GatewayAdvancedSpell（跨维度结构传送）

## 设计意图

高级版结构传送聚晶。与定点聚晶行为一致，但遍历所有维度搜索最近的结构，支持跨维度传送。

传送完成后进入 Freecam 灵魂出窍模式，玩家摄像机脱离身体自由穿墙飞行 60 秒，身体留在传送点无敌保护。

## 流程

```
施法 → 跨维度搜索结构 → 传送至安全点
                         → 蓝色线框标记边界 (60s)
                         → Freecam 激活 (60s)
                           ├  WASD 穿墙飞行
                           ├  身体无敌 + 位置锁定
                           └  Shift 或超时 → 返回身体
```

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-07-04 | 创建神行聚晶 + GatewayAdvancedSpell | 需求：跨维度结构传送，遍历 server.getAllLevels() | GatewayAdvancedSpell / GatewayUtil / ModConfig.GATEWAY_ADV_COST / ModConfig.GATEWAY_ADV_COOLDOWN |
| 2026-07-07 | 新增 Freecam 灵魂出窍模式 | 替代线框无法透视的问题，玩家可自由穿墙探索结构 | FreecamHandler / FreecamInputHandler / CameraMixin / FreecamEvents / S2CStartFreecamPacket / C2SFreecamEndPacket / ModConfig.FREECAM_DURATION / ModConfig.FREECAM_SPEED |
