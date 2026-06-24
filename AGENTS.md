## Agent skills

### Issue tracker

Local markdown — issues 存放在 `.scratch/<feature-slug>/` 下。参见 `docs/agents/issue-tracker.md`。

### Triage labels

默认 label 名称。参见 `docs/agents/triage-labels.md`。

### Domain docs

Single-context repo。参见 `docs/agents/domain.md`。

### 可用技能

| skill | 触发关键词 |
|-------|-----------|
| create-dev | Create machines / Ponder / kinetic BE / BESR |
| entity-render-dev | entity render / collision / invisible |
| goety-addon-dev | Goety Focus / Spell / servant / soul / 聚晶 / 法术 / 仆从 / 灵魂 |
| goety-block-anim-and-particle | block anim / particle / camera shake |
| goetypunk-architecture | package / convention / register |
| jei-dev | JEI / recipe display / ghost drag |
| patchouli-dev | Patchouli / guide book / page |
| prototype-charge-jump | charge jump / jump height |
| prototype-flight-mixin | elytra flight / Mixin / velocity |
| openspec-propose | explore / design / spec |
| openspec-apply-change | implement / build / fix task |
| grill-me | discussion / plan / design |
| grill-with-docs | new term / concept / ADR |

## 核心工作流（高优先级，每条规则均为 IF → THEN 格式）

### 方案先行

IF 用户说「分析 / 审查 / 讨论 / 方案 / 找方案」 THEN 只输出方案不写代码。

IF 用户说「修复 / 改代码 / 实施 / 开始做 / 动手 / 执行 / 写代码」 THEN 此规则解除，进入实现阶段。

### Openspec 流程强制

IF 已进入实现阶段 THEN 调用 openspec-apply-change skill 走完整 openspec 工作流，不得直接编辑文件。

例外：5 行以内的 trivial 修复可以直接改。

### Skill 优先于源码

IF 讨论/开发涉及可用技能表中任一 skill 覆盖的领域（如聚晶/法术、方块动画、JEI 配方等） THEN 必须先调用 `skill` tool 加载对应 skill，再从 skill 文档中确认约定和模式，最后才读源码验证。

禁止跳过 skill 直接读源码。skill 文档是约定层，源码是验证层，两者都要但不允许跳过约定层。

### 运行验证（每次改完代码自动 runClient）

IF auto-commit 包含 `src/` 下的生产代码变更（`.java` 文件） THEN 自动执行 runClient 验证：

1. `Start-Process -NoNewWindow gradlew runClient`
2. `Wait-Process -Timeout 240` 等 240 秒
3. 如果进程退出 → 立即读 `run/logs/latest.log`
4. 如果 240 秒超时 → `Stop-Process -Force` 杀进程 → 读日志
5. 搜索 `ERROR / CRASH / Exception / Caused by / Mixin`
6. 无错误 → 告诉用户"编译启动通过，可以进游戏测试了"
7. 有错误 → 按以下流程处理：
   a. **crash 日志自动快照**：`Copy-Item run/logs/latest.log "run/logs/crash-snapshot/$(Get-Date -Format 'yyyyMMdd-HHmmss')-$(Select-String -Pattern 'Caused by|at (com\.goetypunk|net\.minecraft)' run/logs/latest.log | Select-Object -First 1 | ForEach-Object { $_ -replace '.*\.(\w+)\..*', '$1' }).log"`
   b. **调用 diagnose skill 分析根因**（必须，不得跳过）
   c. **禁止在分析出根因前删除**任何文件（日志 / 存档 / 配置）
   d. 贴出报错摘要 + 根因分析 + 修复建议或无法修复的原因

不触发 runClient 的排除项：仅修改 `docs/`、`AGENTS.md`、`*.json`、`*.yaml`、`*.yml`、`*.toml` 等非代码文件。

### 即时错误记录

IF 调试超过 **15 分钟**未定位、且根因非 trivial THEN 压缩上下文或关闭对话前写入 `docs/incidents/<date>-<slug>.md`。

- 「15 分钟」= 跨对话累计纯调试时间
- 同一根因的多次尝试 = 一个错误，不重复写
- 记录头部含 `status: verified | disputed | superseded`

### 物品文档自动记录

IF 任何代码修改涉及已注册物品/方块/实体 THEN 自动在 `docs/items/<category>/<item-slug>.md` 追一条 timeline 记录（格式：改动 / 原因 / 关联）。

触发条件不只是 openspec task 完成，还包括：
- 直接编辑了物品/方块/实体的 Java 源文件
- 修改了注册类（ModItems/ModBlocks/ModEntities）
- 新建了物品/方块/实体的 Java 类文件
- openspec-apply-change 标记 task 完成

### 压缩前文档检查（上下文即将压缩时强制执行）

IF 需要压缩上下文 / 关闭对话 / 切换话题 THEN AI 必须自动执行以下检查清单：

1. 读取 `.opencode/.session-changes.json`，检查所有 `docWritten: false` 的物品变更记录。对应的 `docs/items/` 文件必须补写 timeline 后标记 `docWritten: true`。
2. 检查是否有未记录的调试经验。累计 ≥15 分钟的写入 `docs/incidents/`。
3. 检查是否有架构/流程相关教训（比如这次 runClient 规则的设计失误），写入 `docs/incidents/`。
4. 全部完成后才能执行压缩。

### 用户说「归档」时做什么

IF 用户说「归档 / 记录一下 / 记下来」THEN：
- 如果有未记录的物品改动 → 更新 `docs/items/` 对应 timeline
- 如果有调试经验或教训 → 写 `docs/incidents/`
- 如果涉及架构决策 → 更新 `docs/agents/` 或写 ADR

不触发 openspec archive-change（那是不同的动作）。

### 自动追问

IF 用户发起讨论/探讨/分析 THEN 自动调用 grill-me skill。

IF 讨论中出现 CONTEXT.md 未记录的新术语或 new concept THEN 自动升级为 grill-with-docs。

## 🚫 禁令

### 语言：始终使用中文

无论上下文压缩、模式切换或打断，所有回复必须使用简体中文（代码/日志/技术术语保持原文）。

### 存档/运行时数据保护

**以任何理由创建、修改或删除存档/运行时数据文件均被严格禁止，无论用户是否在场、是否要求、是否紧急。此禁令不可逾越。**

### 受保护路径模式

任何路径匹配以下模式的均在保护范围内：

| 模式 | 说明 |
|------|------|
| `**/saves/**` | Minecraft 存档目录 |
| `**/playerdata/**` | 玩家数据 |
| `**/region/**` | 区块文件 |
| `**/poi/**` | 兴趣点数据 |
| `**/entities/**` | 实体数据 |
| `**/level.dat` | 世界数据文件 |
| `**/level.dat_old` | 世界数据备份 |
| `**/DIM-?[0-9]**` | 维度目录 |
| `**/session.lock` | 会话锁文件 |

### 强制执行机制（不可绕过）

此禁令有三层强制执行：

1. **Runtime plugin** (`.opencode/plugins/save-guard.ts`) — 在所有工具（bash/write/edit）执行前检查输入参数，匹配受保护路径立即抛出错误中止
2. **opencode.json permission** — 对包含受保护路径的 bash 命令设置 `"ask"` 权限，需要用户手动确认
3. **本指令** — AI 必须主动遵守，违反将导致不可逆后果

### 合法操作示例（仍可能触发 plugin 检查，需用户确认）

- 检查存档是否存在（`Get-ChildItem run/saves/` — 会触发 permission ask）
- 检查 level.dat 是否存在问题

### 免责

plugin 是最终防线。若 plugin 出错导致操作未被拦截，**AI 仍需为违规行为负责**。

---

### 删除保护：自动快照（全局强制执行）

**任何涉及文件/目录删除的操作，必须先自动创建 Git 分支快照。**

流程：
1. 检测到删除操作（`Remove-Item`、`rm`、`del`、`git clean`、`git reset --hard`、文件覆写等）
2. 自动执行快照流程：`git add -A && git stash && git checkout -b backup-<N> && git stash pop && git add -A && git commit -m "auto-snapshot backup-N"`
3. 最多保留 10 个快照分支（`backup-0` ~ `backup-9`），超限自动删除最旧
4. 告知用户快照分支名
5. 然后在原分支上执行删除操作

强制执行见 `~/.config/opencode/instructions/delete-guard.md` 和 `~/.config/opencode/plugins/delete-guard.ts`。

### 每次修改自动提交

**每次 `edit` / `write` 操作完成后，必须立即执行 git 提交。**

格式：
```bash
git add -A
git commit --author="opencode <autocommit>" -m "[auto] <tool>: <filepath> | <diff summary>"
```

示例：
```
[auto] edit: src/Foo.java | 1 file changed, 15 insertions(+), 3 deletions(-)
[auto] write: src/Bar.java
```

规则：
- 使用专用 author `opencode <autocommit>`，与人工 commit 区分
- commit 消息统一 `[auto]` 前缀
- 包含文件名和改动的简要描述
- 无变更时不创建空 commit
- 人工 commit 不受影响（无 `[auto]` 前缀的不是 auto-commit）

### 为什么

opencode 的 undo 只能回退整个会话。auto-commit 后，`git log --author=opencode --oneline` 可看到每次修改的精确记录，`git revert <hash>` 可精准回退单次改动。

### 查看 auto-commit 历史

```bash
git log --author=opencode --oneline
```

### 回退某次 auto-commit

```bash
git revert <commit-hash>
```

## ⚡ 全局规则：优先使用 Libcode 源码而非反编译

**所有前置模组的完整源代码在 `D:\Libcode\` 目录下。任何需要查看依赖模组代码的操作（分析、修复、调试），必须优先到 `D:\Libcode\` 查找源文件，而不是反编译 jar。** 参见 `CONTEXT.md` 和 `.opencode/skills/_shared/libcode-paths.md`。
