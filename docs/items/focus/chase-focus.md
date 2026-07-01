# 追猎聚晶 — GoetyMech

## 身份

- 类型：focus
- 注册类：ModItems.CHASE_FOCUS
- Java 类：ChaseFocus (extends MagicFocus)
- 注册 ID：`GoetyMech:chase_focus`
- 前身：`void_blink_focus`（虚空闪现聚晶）
- 底层法术：VoidBlinkSpell（锁定+多次闪现传送）

## 设计意图

远程战斗聚晶：锁定目标后施加虚空印记，玩家可在窗口期内多次按 Shift 闪现到目标背后。原名"虚空闪现聚晶"，后更名为"追猎聚晶"匹配新主题。

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 从 `void_blink_focus` 重命名为 `chase_focus`，替换纹理为 `tracking_eye_orb_pupil.png` | 主题改为追猎视觉风格，英文名 Chase Focus 契合 Goety 命名惯例 | ModItems / ChaseFocus / 语言文件 / ModConfig / ModCreativeTab |
| 2026-07-02 | 底层法术 VoidBlinkSpell 重构为锁定+多次闪现设计：长按锁定→GLOWING 高亮→VoidMark 效果→Shift 触发传送。ChaseFocus 无代码改动，仍通过构造传入 VoidBlinkSpell | 增强玩法深度，从简单传送改为完整的锁定→标记→闪现交互链 | VoidBlinkSpell / VoidMarkEffect / VoidBlinkEvents / C2SVoidBlinkPacket / VoidBlinkFocus |
