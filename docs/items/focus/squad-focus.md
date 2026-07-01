# 兵团聚晶 — GoetyMech

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

### 2026-06-24 — 优化：rightStaff + cooldown 守卫

- 改动：添加 `rightStaff(staff)` → 法杖匹配时召唤寿命 ×1.5；cooldown 添加 `isEmpty()` 守卫
- 原因：符合 Goety Spell 规范，修复自动施法路径下潜在的空聚晶冷却问题
- 关联：SquadSpell SpellResult
