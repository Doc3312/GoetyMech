# chase-focus Timeline

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-07-18 | 添加 appendHoverText + shift_info 描述 | 对齐 Goety 聚晶 SHIFT 说明规范 | — |
| 2026-06-24 | 从 `void_blink_focus` 重命名为 `chase_focus`，替换纹理为 `tracking_eye_orb_pupil.png` | 主题改为追猎视觉风格，英文名 Chase Focus 契合 Goety 命名惯例 | ModItems / ChaseFocus / 语言文件 / ModConfig / ModCreativeTab |
| 2026-07-02 | 底层法术 VoidBlinkSpell 重构为锁定+多次闪现设计：长按锁定→GLOWING 高亮→VoidMark 效果→Shift 触发传送。ChaseFocus 无代码改动，仍通过构造传入 VoidBlinkSpell | 增强玩法深度，从简单传送改为完整的锁定→标记→闪现交互链 | VoidBlinkSpell / VoidMarkEffect / VoidBlinkEvents / C2SVoidBlinkPacket / VoidBlinkFocus |
| 2026-07-13 | 配置调整：soul_cost 140→100，cooldown 3600→160(8s) | 降低门槛，缩短冷却 | ModConfig |
| 2026-07-13 | Override getTarget() 支持 PartEntity(多碰撞箱实体) | 修复末影龙等无法锁定的问题 | VoidBlinkSpell / incident: chase.PartEntityTargeting |

\n## 2026-07-13\n
- **2026-07-13** — 添加合成配方 (chase_focus.json)
  - 原因：补全生存获取途径
- **2026-07-14** — 功能冻结归档
  - 已完成功能：修复 buff 刷新失效
  - 根因：C2SVoidBlinkPacket 中 `effect.getDuration() / 20 * 20` 整数除法截断 → 新时长 ≤ 原时长 →  MC `addEffect` 内部 `MobEffectInstance.update()` 静默忽略
  - 修复：SpellResult 保存 `TAG_WINDOW_DURATION`（满窗口时长），闪现时读取并重置到满时长
  - 关联 incidents：chase-focus/20260714.buff-refresh-silent-fail
- **2026-07-28** — 语义修正：VoidBlinkSpell → ChaseSpell，VoidBlinkEvents → ChaseEvents，C2SVoidBlinkPacket → C2SChasePacket，ModConfig VOID_BLINK_* → CHASE_*
  - 原因：类名与物品名 chase_focus 语义对齐（"虚空闪现"不反映"追猎"主题）
  - 影响：ChaseSpell / ChaseEvents / C2SChasePacket / ModConfig / ModNetwork / GoetyMechMod
  - Cooldown discount 已在此前正确实现（L141-142: `* ModAttributes.getCooldownDiscount(caster)`），不属本次修复范围
- **2026-07-28** — 全物品冷却折扣补全
  - 检查了项目中所有 12 个 Spell，发现 7 个在手动 `SEHelper.addCooldown` 时未乘 `ModAttributes.getCooldownDiscount()`
  - 修复：CrushingWheelSpell / GatewaySpell / GatewayAdvancedSpell / StableRiftSpell / UnstableRiftSpell / RemoteStorageSpell / SquadSpell
  - 已正确（无需修改）：VoidBlinkSpell(Chase) / SculkBindSpell / StressShieldSpell / SoulDominionSpell / CrazedSummonSpell
- **2026-07-28** — 功能冻结归档
  - 已完成功能：全 Spell 冷却折扣补全 + VoidBlink→Chase 语义修正
  - 关联 incidents：_meta/20260728.cooldown-discount-pattern