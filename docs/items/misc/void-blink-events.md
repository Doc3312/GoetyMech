# Void Blink Events

虚空闪现事件处理。
- **服务端**: PlayerTickEvent 检查 VoidMark 效果窗口过期 → 清理；LivingDeathEvent 检测目标死亡 → 清理所有关联玩家的 VoidMark。
- **客户端**: ClientTickEvent 检测 Shift 按下 → 发送 C2SVoidBlinkPacket。

## Timeline
- **2026-06-24** — 初始实现。
- **2026-07-02** — 重构为新的事件监听：客户端 Shift 检测发包 + 服务端过期清理 + 目标死亡清理。
