# stress-focus Timeline

### 2026-06-24 — 优化：rightStaff + cooldown 守卫

- 改动：添加 `rightStaff(staff)` → 法杖匹配时碾压轮伤害 ×1.5；cooldown 添加 `isEmpty()` 守卫
- 原因：符合 Goety Spell 规范，修复自动施法路径下潜在的空聚晶冷却问题
- 关联：CrushingWheelSpell SpellResult

### 2026-07-09 — Ark 优先读取

- 改动：add `hasArk()` + `getAggregatedCapacity()` 到 GlobalStressLinkManager；CrushingWheelSpell 改为 Ark 优先 → bound 回退；tooltip 从客户端 StressArkHUD 动态计算伤害值
- 原因：有 Ark 时无需手动绑定 Reader，优先读取聚合容量
- 关联：CrushingWheelSpell, StressFocus, GlobalStressLinkManager
