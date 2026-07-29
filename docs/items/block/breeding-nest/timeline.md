# Breeding Nest Timeline

- **2026-06-29** — 初始创建
- **2026-06-29** — 输入冷却 + 满仓提取修复

\n## 2026-07-13\n
- **2026-07-13** — 添加合成配方 (breeding_nest.json)
  - 原因：补全生存获取途径

- **2026-07-17** — 仪式灵魂消耗调整
  - 改动：breeding_nest.json soulCost 1000 → 500
  - 原因：平衡性调整

- **2026-07-28** — 修复挖掘标签 + loot table
  - 改动：加入 mineable/pickaxe 标签；loot table 从空池改为掉落自身
  - 原因：之前 loot table 错误地设为 `pools: []`，破坏后不掉落

## 合并自 breeding_nest

# 育巢开发时间线

- **2026-07-23** — 功能冻结归档
  - 已完成功能：
    - 下方漏斗可提取产物（`getCapability` 放开 DOWN + `canTakeItemThroughFace` 允许 DOWN）
    - 转化完成音效 `VILLAGER_CELEBRATE` → 凋零系列音效（最终定稿 `WITHER_AMBIENT`）
    - HUD: "育巢充能中" → "育巢饱和度"，删除 hunger 数字行
    - 空手/非食物右键可取出产物
    - **有产物时优先弹出**（即使手持食物也先提取再插入）

- **2026-07-28** — 修复挖掘标签 + loot table
  - 改动：加入 mineable/pickaxe 标签；loot table 从空池改为掉落自身
  - 原因：之前 loot table 错误地设为 `pools: []`，破坏后不掉落
