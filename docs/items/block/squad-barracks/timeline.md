# Squad Barracks Timeline

- **2026-06-14** — 初始创建
- **2026-06-15** — 三阶实现 + GUI
- **2026-06-16** — Codebase Remediation: SquadChunkTickets 移动
- **2026-06-26** — 模型替换 + 三阶独立纹理
- **2026-06-26~07-02** — 6 轮模型修复
- **2026-07-27** — 移除服务端 5-tick 速率限制（配置清除不再残留）；新增左键拖拽批量 set/clear

- **2026-07-28** — 修复挖掘标签 + loot table
  - 改动：加入 mineable/pickaxe 标签；新增 tier1/2/3 战利品表（掉落自身）
  - 原因：requiresCorrectToolForDrops() 但无 mineable 标签 → 破坏不掉落
