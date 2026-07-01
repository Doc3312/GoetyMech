# Stress Shield (Spell)

Stress Shield 法术。施放后获得应力护盾。

## Timeline
- **2026-06-24** — 初始实现。
- **2026-07-02** — 重构为完整 Capability 方案：IStressShield + StressShieldProvider + StressShieldHelper；新增被动恢复（5s 无伤后每秒恢复 20%）；新增 StressShieldFocus 聚晶物品（独立绑定 NBT）；新增 StressReaderBlock 绑定支持；注册到创造栏。
