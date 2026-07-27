# gateway-focus-advanced Timeline

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-07-28 | 渐进式结构搜索重构 | 修复跨维搜索优先 + 阻塞式 findNearestMapStructure 性能问题 | GatewaySearchWorker/GatewaySearchManager/ModConfig |
| 2026-07-28 | 修复传送位置偏差 | 替换 findSafeTeleportPos 为 findSafeNearStructure（多级高度图搜索，螺旋 0→20 格）| GatewayUtil/GatewaySearchManager/GatewayAdvancedSpell |
| 2026-07-18 | 添加 appendHoverText + shift_info 描述 | 对齐 Goety 聚晶 SHIFT 说明规范 | — |
| 2026-07-09 | 属性冻结归档 | 5秒施法 + VOID粒子 + heightmap贴地安全搜索 + 地图定位 + FreecamActive标记 | GatewayAdvancedSpell/GatewayUtil/ModCreativeTab/S2CSetFreecamPacket |

## 2026-07-13

- **2026-07-13** — 添加合成配方 (gateway_focus_advanced.json)
  - 原因：补全生存获取途径

## 2026-07-15

- **纹理替换**：`gateway_focus_advanced.png` 从 Desktop `teleport_freedom.png` 覆盖