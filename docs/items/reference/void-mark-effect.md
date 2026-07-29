# Void Mark Effect

虚空印记 MobEffect（BUFF 类型，紫色 #4400AA）。作为"闪现预备"状态的标记，实际数据（目标UUID、剩余传送次数、窗口结束时间）存储在玩家 PersistentData 中。

PersistentData 标签（NBT key 为内部格式，与类名解耦，不改动）：
- `GoetyMech.void_blink_target` — 目标 UUID
- `GoetyMech.void_blink_remaining` — 剩余传送次数
- `GoetyMech.void_blink_window_end` — 窗口结束 game time

## Timeline
- **2026-06-24** — 初始实现。
- **2026-07-02** — 重构为数据容器角色：Effect 本身仅作状态标记，数据全部迁移到 PersistentData。新增 TAG_TARGET/TAG_REMAINING/TAG_WINDOW_END。
