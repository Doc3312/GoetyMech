---
name: "closeout-feature"
description: "归档一个已完成的功能，记录 timeline + 经验 + incidents 关联"
---

# Feature Closeout Protocol

> 当一项功能的实现暂时告一段落时使用。把有价值的部分记录下来，不留僵尸文件。

## 触发条件

以下情况之一触发 closeout：
- 用户明确说「归档」
- openspec change 标记为 complete（达到全部 task）
- 一个 item/block/entity 不再活跃开发（物理域重构等）

## Protocol（4 步）

### Step 1 — Timeline 追加

往对应的 `docs/items/<category>/<item>/timeline.md` 追加一条归档记录：
- 含日期 + 变更摘要
- 指向对应的 openspec change（如果有）
- 指向关联的 incidents

格式：
```
- **YYYY-MM-DD** — 功能冻结归档
  - 已完成功能：...
  - 关联 openspec：<change-name>
  - 关联 incidents：<domain.YYYYMMDD.slug.md>
```

### Step 2 — 经验提取

审查开发过程中遇到的所有问题，如果有未记录的教训：
- 写入 `docs/incidents/<domain>.<YYYYMMDD>.<slug>.md`
- 如果已有对应 incident（日期接近），追加到现有文件
- 如果没有值得记录的经验，跳过

### Step 3 — Incident 索引更新

如果该物品有 incidents 索引文件 `docs/items/<category>/<item>/incidents.md`：
- 追加本次归档新产生的或关联的 incident 链接
- 如果没有 incidents.md，创建它

### Step 4 — 整理清理

- 如果功能开发过程中产生了临时 GameTest / JUnit 测试文件，按用户要求删除或保留
- 如果临时 skill 或文档可清理，一并处理
- 如果涉及 `docs/skills/` 的经验文档，确认已写入

---

## 与 openspec-archive-change 的关系

```
openspec-archive-change          closeout-feature（本 skill）
────────────────────────────     ────────────────────────────
管 change 本身：git tag         管知识记录
清理 task 状态                  写入 timeline / incidents / 经验
机械操作                        知识管理

调用顺序：先 closeout-feature → 再 openspec-archive-change
```
