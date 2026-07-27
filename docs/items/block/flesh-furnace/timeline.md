# Flesh Furnace Timeline

- **2026-06-14** — 初始创建
- **2026-06-27** — C1 handler 索引修复
- **2026-06-29** — stack.split 引用修复 + cachedLinkRange 初始化修复
- **2026-07-13** — 添加合成配方 (flesh_furnace.json)
- **2026-07-26** — 归档：scanAndLink 改为最近火炬优先
  - 修复：原先按 x→y→z 遍历顺序返回第一个匹配火炬，改为收集所有候选后选 distSqr 最小的
  - 关联 openspec：无（直接修复）

- **2026-07-28** — 修复 mineable/pickaxe 标签
  - 改动：加入 mineable/pickaxe 标签（未设置 requiresCorrectToolForDrops，可空手挖掘）
  - 原因：无标签时镐挖掘无速度加成