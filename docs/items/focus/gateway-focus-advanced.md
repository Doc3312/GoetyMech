# 神行聚晶 — GoetyMech

## 身份

- 类型：focus (advanced)
- 注册类：ModItems.GATEWAY_FOCUS_ADVANCED
- Java 类：GatewayAdvancedFocus (extends MagicFocus)
- 注册 ID：`goetymech:gateway_focus_advanced`
- 底层法术：GatewayAdvancedSpell（跨维度结构传送）

## 设计意图

高级版结构传送聚晶。与定点聚晶行为一致，但遍历所有维度搜索最近的结构，支持跨维度传送。

传送完成后进入 Freecam 灵魂出窍模式（基于原版旁观者 GameType.SPECTATOR），玩家以旁观者身份穿墙飞行 60 秒，身体留在传送点，结束后自动恢复原游戏模式并传送回身体。同时获得夜视效果，便于观察结构。

## 流程

```
施法 → 跨维度搜索结构 → 传送至安全点
                         → 旁观者模式激活 (60s) + 夜视
                           ├  原版 WASD/Jump/Shift 自由飞越
                           ├  原版碰撞/穿墙/无重力
                           ├  屏幕上方红色提示 "按R退出神行状态"
                           └  R 或超时 → 恢复原模式 + 传送回身体
```

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-07-04 | 创建神行聚晶 + GatewayAdvancedSpell | 需求：跨维度结构传送，遍历 server.getAllLevels() | GatewayAdvancedSpell / GatewayUtil / ModConfig.GATEWAY_ADV_COST / ModConfig.GATEWAY_ADV_COOLDOWN |
| 2026-07-07 | 新增 Freecam 灵魂出窍模式 | 替代线框无法透视的问题，玩家可自由穿墙探索结构 | FreecamHandler / FreecamInputHandler / CameraMixin / FreecamEvents / S2CStartFreecamPacket / C2SFreecamEndPacket / ModConfig.FREECAM_DURATION / ModConfig.FREECAM_SPEED |
| 2026-07-07 | 删除线框渲染（StructureHighlightRenderer/Marker）；Freecam 增加夜视效果 | 线框无实际作用，夜视帮助观察结构内部 | GatewayAdvancedSpell |
| 2026-07-07 | Freecam 移动平滑化 + 退出键 Shift→R + 红色 HUD 提示 | 修复卡顿，Shift 冲突，增加退出可见性 | FreecamHandler / FreecamInputHandler / CameraMixin / FreecamHUD / ModClientEvents |
| 2026-07-07 | 重构为原版旁观者模式，删除自定义 Freecam 管线（4个文件） | 自定义渲染层卡顿不可修复，原版 spectator 零行移动代码 | FreecamSpectatorHandler / CameraMixin / FreecamHUD / ModClientForgeEvents |
| 2026-07-07 | deferred deactivate + abilities 强制重置；F5/旁观者菜单吞噬 | spectator 退出后玩家飘在空中/无法跳跃；掉帧 | FreecamSpectatorHandler / ModClientForgeEvents / CameraMixin |
