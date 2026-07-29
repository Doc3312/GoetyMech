# 文档系统导航

## 我想知道...

| 问题 | 查什么 |
|------|--------|
| 这个物品是干什么的 | docs/items/<category>/<item>/spec.md → 功能概述 |
| 这个物品怎么实现的 | spec.md → 实现要点 + 外部参考 |
| 这个物品改过什么 | timeline.md 表格 |
| 这个物品出过什么 bug | docs/INDEX.yaml 查 related_incidents |
| 这类 bug 之前出现过吗 | docs/patterns/ 搜相关 tag |
| 为什么架构这样设计 | docs/design/adr/ |
| 项目文档整体健康吗 | node .opencode/scripts/health-check.mjs |
| 我应该建什么文档 | 看下面决策矩阵 |

## 文档决策矩阵

| 我做了 | 我应该建/更新 |
|--------|-------------|
| 新建了一个物品类(注册到ModItems) | spec.md + timeline.md（用 new-item-doc.mjs 脚手架）|
| 修改了物品代码 | timeline.md 追加一行表格记录 |
| 修了一个 bug | 创建 incident.md（RCA模板）→ 如果同类≥3则提议创建 pattern |
| 做了一个架构决策 | ADR |
| 归档功能 | timeline.md + incident审核 + pattern提案检查 |
| 写临时脚本 | .opencode/tmp/<name> |
| 反编译前置mod源码（Libcode找不到时）| .opencode/decompiled/<modname>/ |

## 文档结构

```
docs/
  INDEX.yaml          # 构建产物，不可手编
  items/              # 物品/方块/实体文档
    {category}/{item}/spec.md     # 6章：元数据/概述/设计意图/实现要点/外部参考/关键交互
    {category}/{item}/timeline.md # 5列表格：日期时间/类型/改动/文件/关联
  incidents/          # RCA事件记录
    {subdir}/{id}.md  # YAML front matter + 症状/根因/修复/经验提炼
  patterns/           # 可复用代码模式
    {slug}.md         # ≥3同类incident触发创建
  design/adr/         # 架构决策记录
```

## 脚本速查

| 脚本 | 用途 |
|------|------|
| node .opencode/scripts/build-index.mjs | 重建 INDEX.yaml |
| node .opencode/scripts/validate-index.mjs | 校验索引一致性 |
| node .opencode/scripts/health-check.mjs | 红绿灯仪表盘 |
| node .opencode/scripts/new-item-doc.mjs <cat> <item> | 创建物品文档脚手架 |
