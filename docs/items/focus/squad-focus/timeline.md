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

## 2026-07-14 — 大规模架构重构

  - 改动：
    - **Plan A：Spell 架构对齐 SummonSpell 模式** — shift dismiss 走长按施法流程（cannotCast→SpellResult→isShifting），删除 DarkWandUseMixin 的点按拦截
    - **冷却系统回归 Goety 标准** — `SEHelper.addCooldown` 替代自定义 NBT 冷却，删除 `conditionsMet` 重写
    - **跨维度实体修复** — `type.create(worldIn)` + `worldIn.addFreshEntity(mob)` 解决末地/下界召唤实体不出现
    - **丰碑解除跨维限制** — dimension check 排除 monument 绑定，任意阶可跨维召唤
    - **召唤/死亡特效** — 自定义 `ScaledGodRayParticle` 光柱粒子按实体碰撞箱缩放；`SOUL` + `summonUndeadParticles` 灵魂特效；`WITHER_DEATH` 音效
    - **HUD 实体名字** — BE 新增 `savedEntityNames` 字段 + `.dat` 向后兼容回填
    - **仆从行为** — 默认跟随模式 + `MonumentFollowGoal`（非 IServant 实体）；`LivingHurtEvent` 监听攻击目标重定向
    - **Tooltip/文本** — 丰碑消耗 `MONUMENT_SOUL_COST=1000` 每单位；解绑消息区分 monument/barracks；`empty_squad_monument` 等 3 条 lang 新增
    - **折扣调整** — T2 0.5→0.8，T3 0.1→0.5
    - **灵魂同步修复** — `SEHelper.sendSEUpdatePacket` 扣魂后发包，解决客户端魂数不更新
    - **monument cache key 修复** — 写入 key 从 `cached_count` 改为 `monument_used`，tooltip 实时更新
  - 原因：多轮讨论确认 Goety SummonSpell 标准模式；自定义 NBT 冷却偏离框架规范
  - 关联 incidents：`_craft.20260714.squad-monument-architecture.md`

### 2026-07-13 — 合成配方
- **2026-07-13** — 添加合成配方 (squad_focus.json)
  - 原因：补全生存获取途径