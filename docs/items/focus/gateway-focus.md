# 定点聚晶 — GoetyMech

## 身份

- 类型：focus (basic)
- 注册类：ModItems.GATEWAY_FOCUS
- Java 类：GatewayFocus (extends MagicFocus)
- 注册 ID：`goetymech:gateway_focus`
- 底层法术：GatewaySpell（同维度结构传送）

## 设计意图

基础版结构传送聚晶。副手持结构之眼，主手持杖施法后传送到结构附近安全位置。仅限同维度搜索。

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-07-04 | 创建定点聚晶 + GatewaySpell + GatewayUtil | 需求：副手眼道具定位结构，聚晶传送到结构附近20m安全点 | GatewaySpell / GatewayUtil / ModConfig.GATEWAY_COST / ModConfig.GATEWAY_COOLDOWN |
