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

## 合并自 gateway_focus_advanced

# Gateway Advanced Focus — Timeline

- **2026-07-20** — 结构扫描机制重设计归档
  - 废弃：跨维度的 `deriveStructureTag` + 手动 tag 推导 + namespace 扫描
  - 重写：先用 `offhand.use()` 触发眼的本机搜索，同时 Mixin 注入 `ServerLevel.findNearestMapStructure(TagKey)` 捕获结构 tag
  - 新增：`ServerLevelFindStructureMixin`
  - 双阶段策略：
    1. 有 captured tag → 遍历 `Server.getAllLevels()`，手动搜索最近结构（跨维）
    2. 无 captured tag（某些 mod 调 HolderSet 重载）→ 回退到 signalTo pos（单维）
  - 关联 incidents：`_craft.20260720.gateway-offhand-use.md`
