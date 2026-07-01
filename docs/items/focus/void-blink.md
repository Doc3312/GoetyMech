# Void Blink (Spell)

虚空闪现法术。长按 1s 锁定目标 → 目标高亮 → 玩家获得 VoidMark 效果，期间按 Shift 传送到目标身后。

## Timeline
- **2026-06-24** — 初始实现。
- **2026-07-02** — 重构为锁定+多次闪现设计。新流程：射线锁定目标 → GLOWING 高亮 → VoidMark 效果（存目标UUID/剩余次数/窗口时间） → Shift 触发传送。支持 Potency（+次数）、Duration（+窗口）、Range（+距离）附魔。
