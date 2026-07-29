---
adr_id: "003"
title: "Harness 架构：3插件结构 + fail-open/fail-closed 策略"
status: accepted
date: 2026-07-30
---

# ADR-003: Harness 架构：3插件结构 + fail-open/fail-closed 策略

## Context

当前 `.opencode/plugins/` 仅有 2 个插件（`save-guard` + `skill-preloader`），覆盖不全：

- 无命令级别拦截（`rm -rf`、危险 git 操作可直通）
- 无写入守门（根目录污染、临时文件散落、未声索文件修改无人阻止）
- 上下文压缩后 Agent 丢失规则记忆（如 AGENTS.md 的强制规则在长对话后不再生效）

## Decision

重构为 **3 插件结构**，按拦截点分组：

| 插件 | 职责 | 策略 |
|------|------|------|
| `bash-guard` | 命令白名单、auto-commit 注入、runClient 调度 | fail-closed |
| `write-guard` | 文档合规校验、临时文件拦截、声索文件校验 | fail-closed |
| `skill-preloader` | 上下文注入（AGENTS.md 规则重新加载） | fail-open |

- **fail-closed**：插件出错时拒绝操作（安全优先，适用于 bash-guard / write-guard）
- **fail-open**：插件出错时放行（可用性优先，适用于 skill-preloader 上下文注入，避免因注入失败阻断工作流）

## Alternatives Considered

| 方案 | 取舍 |
|------|------|
| 3 插件（选中） | 职责清晰、独立测试；需维护 3 个模块 |
| 单一大插件 | 简单但耦合严重，一个 Guard 出错全停 |
| 更多细粒度插件 | 灵活但管理复杂度高 |

## Consequences

- `runClient` 在有活跃声索文件时跳过（避免未完成代码编译崩溃）
- 插件自身 bug 可阻断所有操作（尤其是 bash-guard 的 fail-closed 策略），需充分测试
- 每个插件的单元测试需覆盖 fail-open / fail-closed 异常路径

## See Also

- `.opencode/plugins/bash-guard.ts`
- `.opencode/plugins/write-guard.ts`
- `.opencode/plugins/skill-preloader.ts`
