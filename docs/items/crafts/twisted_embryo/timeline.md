# 扭曲胚胎 — Timeline

- **2026-06-16** — 提取重复射线追踪逻辑
  - 改动：从 TwistedEmbryoItem.use() 中提取射线追踪定位逻辑到 MinionSpawnHelper.findSpawnPosition()
  - 原因：与 MatureEmbryoItem 存在完全重复的射线追踪代码
  - 关联：openspec codebase-remediation

- **2026-07-16** — Patchouli 条目创建
  - 改动：新建 goetymech_crafts 条目，1页，链接育巢/血肉熔炉，深红引语
  - 关联：20260716.patchouli-percent-format-error
