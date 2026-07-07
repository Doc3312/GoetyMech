# 神行聚晶 — GoetyMech

## 身份

- 类型：focus (advanced)
- 注册类：ModItems.GATEWAY_FOCUS_ADVANCED
- Java 类：GatewayAdvancedFocus (extends MagicFocus)
- 注册 ID：`goetymech:gateway_focus_advanced`
- 底层法术：GatewayAdvancedSpell（跨维度结构传送）

## 设计意图

高级版结构传送聚晶。与定点聚晶行为一致，但遍历所有维度搜索最近的结构，支持跨维度传送。

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-07-04 | 创建神行聚晶 + GatewayAdvancedSpell | 需求：跨维度结构传送，遍历 server.getAllLevels() | GatewayAdvancedSpell / GatewayUtil / ModConfig.GATEWAY_ADV_COST / ModConfig.GATEWAY_ADV_COOLDOWN |
