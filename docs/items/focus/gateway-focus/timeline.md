# gateway-focus Timeline

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-07-18 | 添加 appendHoverText + shift_info 描述 | 对齐 Goety 聚晶 SHIFT 说明规范 | — |
| 2026-07-04 | 创建定点聚晶 + GatewaySpell + GatewayUtil | 需求：副手眼道具定位结构，聚晶传送到结构附近20m安全点 | GatewaySpell / GatewayUtil / ModConfig.GATEWAY_COST / ModConfig.GATEWAY_COOLDOWN |
| 2026-07-07 | 神行聚晶新增 Freecam 灵魂出窍模式（定点聚晶不变） | 玩家传送后可自由穿墙探索结构 | FreecamHandler / CameraMixin / FreecamEvents / ModConfig.FREECAM_DURATION / ModConfig.FREECAM_SPEED |
| 2026-07-07 | 删除 StructureHighlightRenderer + Marker | 线框无实际作用 | GatewayAdvancedSpell / GatewaySpell / ModEntities / ModClientEvents |
| 2026-07-07 | Freecam 移动平滑化（速度驱动+摩擦衰减0.91+partialTick插值） | 原位置驱动导致一卡一顿 | FreecamHandler / FreecamInputHandler / CameraMixin |
| 2026-07-07 | Freecam 退出键 Shift→R，Shift 改为下降 | Shift 退出冲突下降操作；R 键不与其他操作冲突 | FreecamInputHandler / ModClientEvents |
| 2026-07-07 | 新建 FreecamHUD 热力条上方红色提示"按R退出神行状态" | 用户需知道退出方式 | FreecamHUD / ModClientEvents |
| 2026-07-07 | 重构为原版旁观者模式，删除自定义 Freecam 管线 | 自定义渲染层卡顿不可修复，原版 spectator 零行移动代码 | FreecamSpectatorHandler / CameraMixin / FreecamHUD / ModClientForgeEvents |
| 2026-07-07 | deferred deactivate + abilities 强制重置；F5/旁观者菜单吞噬 | spectator 退出后玩家飘在空中/无法跳跃；掉帧 | FreecamSpectatorHandler / ModClientForgeEvents / CameraMixin |
| 2026-07-09 | 属性冻结归档 | 5秒施法 + VOID粒子 + heightmap贴地安全搜索 + 地图定位 + 创造模式标签页 | GatewaySpell/GatewayUtil/ModConfig/GatewayFocus/ModCreativeTab |


## 2026-07-13

- **2026-07-13** — 添加合成配方 (gateway_focus.json)
  - 原因：补全生存获取途径

## 2026-07-15

- **纹理替换**：`gateway_focus.png` 从 Desktop `teleport_anchor.png` 覆盖

## 合并自 gateway_focus

# Gateway Focus — Timeline

- **2026-07-20** — 结构扫描机制重设计归档
  - 废弃：`deriveStructureTag` / `isStructureEyeItem` / `findNearestStructuresInNamespace` / `GatewayMappingLoader` / `ModTags.STRUCTURE_EYES`
  - 重写：改用 `offhand.use()` + Mixin 劫持 `signalTo(BlockPos)` 捕获目标坐标
  - 新增：`EyeOfEnderCaptureMixin`（捕获 vanilla EyeOfEnder.signalTo）
  - 新增：`GoetyEyeItemCaptureMixin`（捕获 Goety EyeItemEntity.signalTo）
  - 新增：`EyeCaptureHandler`（EntityJoinLevelEvent 取消眼睛实体生成）
  - Basic 行为：当前维度单维搜索
  - 关联 incidents：`_craft.20260720.gateway-offhand-use.md`
