# 巫妖晶匣 — 时间线

## 2026-07-17 — 自动施法使用 NamelessStaff 修复

- **背景**：巫妖晶匣（tier 3）自动施法时，`VanguardSpell` 中 `staff.is(NAMELESS_STAFF)` 检查失败
- **根因**：自动施法使用 `AutoCastDummyWand` 作为假法杖，Goety 的 `VanguardSpell` 通过 `staff.is(ModItems.NAMELESS_STAFF.get())` 判断是否为无名法杖，假法杖不是 `NamelessStaff` 实例，走 `else` 分支随机 2~7 个
- **修复**：tier 3 晶匣 + 死灵系聚晶时，创建 `NamelessStaff` ItemStack 代替 `AutoCastDummyWand`，使 `staff.is()` 检查通过，触发最高级召唤路径（固定 7 个）
- **改动文件**：`common/events/FleshCasketEvents.java` — `tryAutoCast()` 方法
- **关联 incident**：`_craft/20260717.staff-is-nameless-pattern.md`
