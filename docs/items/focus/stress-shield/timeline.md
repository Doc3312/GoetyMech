# stress-shield Timeline

- **2026-06-24** — 初始实现。
- **2026-07-02** — 重构为完整 Capability 方案：IStressShield + StressShieldProvider + StressShieldHelper；新增被动恢复（5s 无伤后每秒恢复 20%）；新增 StressShieldFocus 聚晶物品（独立绑定 NBT）；新增 StressReaderBlock 绑定支持；注册到创造栏。
- **2026-07-09** — Ark 优先读取 + 修正为 capacity
  - 改动：`readStress` 改为 Ark 优先 → bound 回退；数据源从 `stress` 修正为 `capacity`；tooltip 从客户端 Ark HUD 动态计算护盾值
  - 原因：与应力聚晶统一行为，有 Ark 时无需绑定
  - 关联：StressShieldSpell, StressShieldFocus, StressShieldHelper
