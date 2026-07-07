# chase-focus Timeline

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 从 `void_blink_focus` 重命名为 `chase_focus`，替换纹理为 `tracking_eye_orb_pupil.png` | 主题改为追猎视觉风格，英文名 Chase Focus 契合 Goety 命名惯例 | ModItems / ChaseFocus / 语言文件 / ModConfig / ModCreativeTab |
| 2026-07-02 | 底层法术 VoidBlinkSpell 重构为锁定+多次闪现设计：长按锁定→GLOWING 高亮→VoidMark 效果→Shift 触发传送。ChaseFocus 无代码改动，仍通过构造传入 VoidBlinkSpell | 增强玩法深度，从简单传送改为完整的锁定→标记→闪现交互链 | VoidBlinkSpell / VoidMarkEffect / VoidBlinkEvents / C2SVoidBlinkPacket / VoidBlinkFocus |
