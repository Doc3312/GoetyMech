# 扭曲胚胎 — Timeline

- **2026-06-16** — 提取重复射线追踪逻辑
  - 改动：从 TwistedEmbryoItem.use() 中提取射线追踪定位逻辑到 MinionSpawnHelper.findSpawnPosition()
  - 原因：与 MatureEmbryoItem 存在完全重复的射线追踪代码
  - 关联：openspec codebase-remediation

- **2026-07-16** — Patchouli 条目创建
  - 改动：新建 goetymech_crafts 条目，1页，链接育巢/血肉熔炉，深红引语
  - 关联：20260716.patchouli-percent-format-error

- **2026-07-17** — JEI 配方页改为信息页
  - 改动：删除 EmbryoConversionCategory/Recipe，改用 addIngredientInfo 展示获取方式说明
  - 原因：胚胎的获取流程不适合用配方页展示，改为文字说明更直观

- **2026-07-17** — 功能冻结归档
  - 已完成：JEI 信息页 + Patchouli 条目 + JEI info 翻译
  - 关联 incidents：无
