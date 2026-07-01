# 应力聚晶 — GoetyMech

## 身份

- 类型：focus
- 注册类：ModItems.STRESS_FOCUS
- Java 类：StressFocus (extends MagicFocus)
- 首次创建：2026-06-14

## 设计意图

绑定 StressReader 方块后，远程释放碾压轮（CrushingWheel）实体攻击。将 Create 的应力系统与战斗法术打通。

## 演化记录

### 2026-06-24 — 优化：rightStaff + cooldown 守卫

- 改动：添加 `rightStaff(staff)` → 法杖匹配时碾压轮伤害 ×1.5；cooldown 添加 `isEmpty()` 守卫
- 原因：符合 Goety Spell 规范，修复自动施法路径下潜在的空聚晶冷却问题
- 关联：CrushingWheelSpell SpellResult
