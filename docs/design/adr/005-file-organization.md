---
adr_id: "005"
title: "文件组织：.opencode/tmp/decompiled/cache 三层临时目录"
status: accepted
date: 2026-07-30
---

# ADR-005: 文件组织：.opencode/tmp/decompiled/cache 三层临时目录

## Context

项目中有三种"非源码"文件存在但无固定存放位置：

- **临时脚本**（一次性调试脚本、迁移脚本）散落在根目录或 `src/` 下
- **反编译代码**（jar 反编译产物）曾被写入项目根目录，污染版本控制
- **构建缓存产物**（INDEX.yaml 等重建产物）无统一缓存目录

## Decision

在 `.opencode/` 下建立 **三层临时目录**，按生命周期区分：

| 目录 | 用途 | 生命周期 | 提交？ |
|------|------|----------|--------|
| `.opencode/tmp/` | 一次性调试脚本、迁移脚本 | 任务结束后可删 | 否 |
| `.opencode/decompiled/` | jar 反编译产物 | 持久缓存 | 否（`.gitignore`） |
| `.opencode/cache/` | 构建/重建产物（INDEX.yaml 等） | 脚本自动管理 | 否 |

**反编译流程规则**：Agent 必须优先到 `D:\Libcode\` 查找源码。只有在 Libcode 中找不到对应类时，才反编译 jar 并将产物写入 `.opencode/decompiled/`。

**根目录保护**：`write-guard` 插件拦截所有对项目根目录（非 `.opencode/` 子目录）的写入操作，确保不再有临时文件污染。

## Alternatives Considered

| 方案 | 取舍 |
|------|------|
| 三层分层（选中） | 区分一次性 vs 持久缓存，清理策略清晰 |
| 全放 `.opencode/tmp/` | 简单但缓存被误删、反编译产物与临时脚本混淆 |
| 放 `build/` 下 | 语义不同（`build/` 是 Gradle 编译输出，不应混入反编译） |

## Consequences

- `.opencode/decompiled/` 不提交版本控制，需在 `.gitignore` 中明确排除
- Agent 必须严格遵守"先查 Libcode、再反编译"的优先级规则
- 旧反编译产物需迁移至 `.opencode/decompiled/`（标记为 TODO）

## See Also

- `D:\Libcode\` — 前置模组完整源码
- `.opencode/plugins/write-guard.ts` — 根目录写入拦截
