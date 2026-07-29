# Review Subagent Prompt

审查任务通过 Task 工具传入本 prompt。审查完成立即返回结果，不保留状态。

## 输入参数

由调用方填入：
- `{{DIFF}}` — `git diff` 输出
- `{{TASK_DESC}}` — openspec tasks.md 中该 task 的描述
- `{{AFFECTED_FILES}}` — 涉及的文件路径列表

## 审查三轴

### 1. Standards
- import 符合规范（无通配符 import，无未使用的 import）
- 命名：字段 camelCase，常量 UPPER_SNAKE，类 PascalCase
- 异常处理：不使用 `Exception` 裸 catch，使用具体异常类型
- 日志：使用 `LOGGER` 而非 `System.out`

### 2. Spec Compliance
- diff 是否实现了 `{{TASK_DESC}}` 描述的功能
- 是否有不属于该 task 的多余改动（YAGNI）
- 是否修改了不应修改的文件（越界修改）

### 3. Project Conventions (GoetyMech)
- 主包 `com.GoetyMech.addon`，禁止使用其他包根
- 注册必须通过 `ModBlocks` / `ModItems` / `ModEntities`，禁止用 `@ObjectHolder`
- Mixin 的 `@At` 必须明确指定 target（method + desc），禁止空/模糊注入
- 所有显示文本必须使用 lang key（`Component.translatable()`），禁止硬编码字符串
- 服务端专用代码必须加 `@OnlyIn(Dist.CLIENT)` 隔离
- 客户端专用代码必须在 `client/` 包下
- 方块实体实现 `WorldlyContainer` 时，必须暴露全方向 `IItemHandler`

## 输出格式

```
## 审查结果: PASS | FAIL | CONDITIONAL_PASS

### 问题列表
1. [P1] <文件:行号> — 问题描述 — 建议修复
2. [P2] ...

### 说明
需要人工判断的疑点、或 PASS 但推荐后续改进的点。
```
