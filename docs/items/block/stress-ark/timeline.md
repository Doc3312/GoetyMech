# Stress Ark Timeline

- **2026-06-14** — 初始创建
- **2026-06-24** — 初始实现
- **2026-07-03** — 中枢重构、分配公式修正、引擎持久性修复、数据包增强
- **2026-07-07** — 重构：删除 getTotalSystemStress()；直接遍历 reader cache 累加
- **2026-07-07** — 消费反馈：refreshStats() 调用 service.getEngineConsumptionForOwner()；syncToClient 透传消耗值；HUD 显示净可用和引擎消耗行
