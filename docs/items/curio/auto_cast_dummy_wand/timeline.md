# 自动施法假人魔杖 — 时间线

## 2026-07-17 — tier 3 自动施法降级使用 NamelessStaff

- **背景**：`AutoCastDummyWand` 不是 `NamelessStaff`，导致 Goety 死灵系法术的 `staff.is(NAMELESS_STAFF)` 检查失败
- **修复**：tier 3 晶匣在死灵系聚晶时，改用 `NamelessStaff` ItemStack 代替 `AutoCastDummyWand`
- **改动文件**：`common/events/FleshCasketEvents.java`
