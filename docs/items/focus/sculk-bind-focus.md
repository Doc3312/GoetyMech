# 幽匿束缚聚晶 — goetypunk

## 身份

- 类型：focus
- 注册类：ModItems.SCULK_BIND_FOCUS
- Java 类：SculkBindFocus (extends MagicFocus)
- 首次创建：2026-06-14

## 设计意图

复合型法术聚晶：锁定目标 → 召唤尖刺 → 声波特效 → 召唤 Warden。对标 Goety 原生聚晶的高级战斗法术。

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 添加 `rightStaff(staff)` → 法杖匹配时多召唤 1 只 Warden；空聚晶守卫 | 符合 Goety Spells 规范；修复 FleshCasket 自动施法路径下对 AIR 上冷却的 bug | SculkBindSpell SpellResult |
