# 不稳定裂隙聚晶 — goetypunk

## 身份

- 类型：focus
- 注册类：ModItems.UNSTABLE_RIFT_FOCUS
- Java 类：UnstableRiftFocus (extends MagicFocus)
- 首次创建：2026-06-14

## 设计意图

创建不稳定的时空裂隙，用于快速位移或传送。与稳定裂隙聚晶形成"临时/永久"配对。

## 演化记录

### 2026-06-24 — 优化：rightStaff + cooldown 守卫

- 改动：添加 `rightStaff(staff)` → 法杖匹配时冷却 ×0.2；cooldown 添加 `isEmpty()` 守卫
- 原因：符合 Goety Spell 规范，修复自动施法路径下潜在的空聚晶冷却问题
- 关联：UnstableRiftSpell SpellResult
