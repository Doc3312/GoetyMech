# Stress Ark Timeline

- **2026-06-14** — 初始创建
- **2026-06-24** — 初始实现
- **2026-07-03** — 中枢重构、分配公式修正、引擎持久性修复、数据包增强
- **2026-07-07** — 重构：删除 getTotalSystemStress()；直接遍历 reader cache 累加
- **2026-07-07** — 消费反馈：refreshStats() 调用 service.getEngineConsumptionForOwner()；syncToClient 透传消耗值；HUD 显示净可用
- **2026-07-07** — HUD 修复：移除引擎消耗文字行（应力条已显示净可用）；sendClearPacket 改用 server 全局 PlayerList 修复跨维度问题

## 待办

- Ark 自动分配 Engine（简化版 auto-pool）
