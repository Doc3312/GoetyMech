# 成熟胚胎 — Timeline

- **2026-06-15** — 引入作为点将台消耗品
  - 改动：MatureEmbryo 被 SquadBarracks 系统引用，作为编队召唤的消耗材料
  - 原因：需要一种可堆叠、可配置的召唤消耗品来驱动点将台
  - 关联：openspec squad-summon-system

- **2026-06-16** — 提取重复射线追踪逻辑
  - 改动：从 MatureEmbryoItem.use() 中提取射线追踪定位逻辑到 MinionSpawnHelper.findSpawnPosition()
  - 原因：与 TwistedEmbryoItem 存在完全重复的射线追踪代码
  - 关联：openspec codebase-remediation

- **2026-07-16** — Patchouli 条目创建
  - 改动：新建 goetymech_crafts 条目，1页，链接扭曲胚胎/血肉熔炉/点将台/兵团聚晶

- **2026-07-17** — JEI 配方页改为信息页
  - 改动：删除 EmbryoConversionCategory/Recipe，改用 addIngredientInfo 展示获取方式说明
  - 原因：胚胎的获取流程不适合用配方页展示，改为文字说明更直观

- **2026-07-17** — 点将台 Patchouli 超链接修正
  - 改动：squad_barracks.json 第1页超链接显示文字 "胚胎" → "成熟胚胎"

- **2026-07-17** — 功能冻结归档
  - 已完成：JEI 信息页 + Patchouli 条目 + 超链接修正
  - 关联 incidents：无
