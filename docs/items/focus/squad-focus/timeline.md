# squad-focus Timeline

### 2026-06-15 — 初始设计与注册

- 改动：创建 SquadFocus + SquadSpell，实现绑定点将台、远程召唤、动态灵魂消耗、编队缓存、5 分钟仆从限时
- 原因：成熟胚胎只能手持右键单次召唤，无法批量编队
- 关联：openspec squad-summon-system

### 2026-06-24 — 优化：rightStaff + cooldown 守卫

- 改动：添加 `rightStaff(staff)` → 法杖匹配时召唤寿命 ×1.5；cooldown 添加 `isEmpty()` 守卫
- 原因：符合 Goety Spell 规范，修复自动施法路径下潜在的空聚晶冷却问题
- 关联：SquadSpell SpellResult

### 2026-07-09 — 丰碑召唤改进

- 改动：追踪存活仆从（全存活→传送，部分死→补召）；丰碑冷却 5min；shift+施法 monument 用 discard（无掉落）；信标 buff 传播 64 格
- 原因：丰碑召唤行为欠缺精细度，需区分 monument/barracks 两种绑定模式
- 关联：openspec monument-summon-improvements

### 2026-07-10 — Bug 修复 + Mixin 重构

- 改动：
  - 冷却 Bug 3：`SquadFocus.use()` 改用 `getFocusCoolDown().isOnCooldown(Item)` 直接查 cooldowns map
  - 补召 Bug 2：添加 `monument_slot` 索引追踪，只补召真正死亡索引的 UUID
  - 死亡掉落 Bug 4：`LivingHurtEvent` 致死拦截 + `LivingDropsEvent` 兜底 → `monument_summoned` 实体无任何掉落
  - Shift+dismiss 重构：`DarkWandUseMixin` 在 `cannotCast()` 之前捕获 SquadFocus shift+右键，直接 dismiss 返回 `sidedSuccess`，不触发 SpellResult 和冷却系统
- 原因：冷却被清除、补召逻辑错误、绑定物掉落绕过 LivingDropsEvent、高温却中无法 dismiss
- 关联：openspec monument-summon-improvements
- 关联 incidents：`_craft.20260710.cooldown-debug.md`

\n## 2026-07-13\n
- **2026-07-13** — 添加合成配方 (squad_focus.json)
  - 原因：补全生存获取途径