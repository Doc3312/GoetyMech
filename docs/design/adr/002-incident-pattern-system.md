---
adr_id: "002"
title: "Incident 体系：RCA 模板 + Pattern 提取 + 四层知识金字塔"
status: accepted
date: 2026-07-30
---

# ADR-002: Incident 体系：RCA 模板 + Pattern 提取 + 四层知识金字塔

## Context

当前 `docs/incidents/` 下有 63 个 incident 文件，存在以下问题：

- **缺乏规范**：6 种不同的 `status` 值、3 种写作格式，机器无法统一解析
- **无关联索引**：incident 与相关物品/实体的 `spec.md` / `timeline.md` 之间无双向链接
- **知识无法复用**：同类错误重复发生但无机制从历史 incident 中提取共有模式

## Decision

采用 **RCA 模板 + Pattern 提取 + 四层知识金字塔** 体系：

1. **Incident = RCA 记录** — 固定模板字段：`症状`、`根因`、`修复`、`经验提炼`、`关联物品`。`status` 统一为四值：`open | verified | superseded | legacy`。
2. **Pattern 提取** — 当 ≥3 个同类 incident 被标记为 `verified` 时，通过 `closeout-feature` 流程触发 Pattern 提取，写入 `docs/design/patterns/`。
3. **四层金字塔** — Incident → Pattern → Skill 文档 → 自动化插件。Pattern 积累足够成熟后升级为 Skill 文档，最终固化为 lint rule 或 guard plugin。
4. **双向关联** — incident front matter 中包含 `related_items`、`related_patterns` 字段；INDEX.yaml 自动建立反向索引。

## Alternatives Considered

| 方案 | 取舍 |
|------|------|
| 内置 RCA 模板（选中） | 结构化但需 Agent 遵守模板 |
| 纯自由格式 | 灵活性好但无法机器聚合 |
| 外部工具（Notion/Jira） | 过重，离代码太远 |

## Consequences

- Pattern 提取依赖 `closeout-feature` 流程触发，若 Agent 跳过归档则 Pattern 永远不生成
- 旧 63 个 incident 需人工复核并打上正确的 `status` 标签（标记为 TODO）
- 新增 `docs/design/patterns/` 目录，需在 INDEX.yaml 中注册

## See Also

- `docs/incidents/` — 已有 incident 库
- `docs/agents/closeout-feature.md` — 归档流程
