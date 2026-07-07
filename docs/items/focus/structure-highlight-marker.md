# 结构高亮标记 — GoetyMech

## 身份

- 类型：entity (client-side marker)
- 注册类：ModEntities.STRUCTURE_HIGHLIGHT_MARKER
- Java 类：StructureHighlightMarker (extends Entity)
- 注册 ID：`goetymech:structure_highlight_marker`
- 渲染器：StructureHighlightRenderer

## 设计意图

纯客户端标记实体，承载结构 bounding box 数据。渲染器绘制白色线框显示结构范围，存活 1200 tick (60s) 后自动消失。实体生命周期由 Entity 系统管理，切维度/下线自动清理。

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-07-04 | 创建 StructureHighlightMarker + StructureHighlightRenderer | 需求：传送后高亮结构范围 60s | GatewaySpell / GatewayAdvancedSpell / ModClientEvents |
