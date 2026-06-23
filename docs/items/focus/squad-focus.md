# 兵团聚晶 — goetypunk

## 身份

- 类型：focus
- 注册类：ModItems.SQUAD_FOCUS
- Java 类：SquadFocus (extends MagicFocus)
- 首次创建：2026-06-14

## 设计意图

远程绑定「点将台」后，消耗胚胎+灵魂批量召唤编队仆从。从"单兵作战"升级到"兵团指挥"。

## 演化记录

### 2026-06-15 — 初始设计与注册

- 改动：创建 SquadFocus + SquadSpell，实现绑定点将台、远程召唤、动态灵魂消耗、编队缓存、5 分钟仆从限时
- 原因：成熟胚胎只能手持右键单次召唤，无法批量编队
- 关联：openspec squad-summon-system
