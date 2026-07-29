---
adr_id: "001"
title: "文档体系：YAML front matter + INDEX.yaml 双轨制"
status: accepted
date: 2026-07-30
---

# ADR-001: 文档体系：YAML front matter + INDEX.yaml 双轨制

## Context

项目文档共计 91 个，散落在多个目录下，无中心索引。`spec.md`、`timeline.md`、`incidents/*.md` 之间彼此不知对方存在，无法做跨文档关联查询。存在 4 组命名重复（同名文件分属不同 item 目录），手工检索效率极低。

## Decision

采用 **YAML front matter + INDEX.yaml 双轨制**：

1. **YAML front matter** — 每个文档头部包含结构化元数据（`item_id`、`category`、`tags`、`related`、`last_modified` 等），由 Agent 在创建/修改时遵循模板填写。
2. **INDEX.yaml** — 为构建产物（单文件），通过脚本从各文档的 front matter 聚合生成，**不可手动编辑**。每次文档变更后由 Agent 或 CI 触发重建。
3. 单一 YAML 格式，不做分层（如 per-directory index），因为项目为单 Agent 项目，跨域查询优先于目录局部性。

## Alternatives Considered

| 方案 | 取舍 |
|------|------|
| 单一 YAML（选中） | 跨域查询方便；需脚本重建 |
| 分层 YAML（per-dir index） | 局部性好但跨域查询需合并 |
| 纯 Markdown（无 front matter） | 简单但无法机器索引 |
| 外部数据库 | 过重，单 Agent 项目不需要 |

## Consequences

- 每次文档新增/删除/修改后必须重建 INDEX.yaml（`node .opencode/scripts/build-index.mjs`）
- 新模板需 Agent 严格遵守 front matter 字段约定
- 需 `validate` 脚本定期校验 front matter 漂移（缺失字段、标签不一致等）

## See Also

- `docs/agents/domain.md` — 文档域定义
- `.opencode/scripts/build-index.mjs` — INDEX 构建脚本
