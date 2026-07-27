# 二阶点将台 — Timeline

## 2026-07-13

- **2026-07-13** — 添加合成配方 (tier2_squad_barracks.json)
  - 原因：补全生存获取途径

- **2026-07-28** — 修复挖掘标签 + loot table
  - 改动：加入 mineable/pickaxe 标签；新增战利品表掉落自身
  - 原因：requiresCorrectToolForDrops() 但无 mineable 标签 → 破坏不掉落
