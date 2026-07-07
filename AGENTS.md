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
| **item-ister-flat-quads** | **★最高优先级★ texture / png / 纹理 / overlay / 叠加层 / quad / 四边面 / ISTER / BEWLR / item/generated / flat item / 物品渲染 / 图标 / scale(1,-1,-1) / UV mapping / vertex / 顶点渲染** |
| create-dev | Create machines / Ponder / kinetic BE / BESR |
| entity-render-dev | entity render / collision / invisible |
| goety-addon-dev | Goety Focus / Spell / servant / soul / 聚晶 / 法术 / 仆从 / 灵魂 |
| goety-block-anim-and-particle | block anim / particle / camera shake |
| GoetyMech-architecture | package / convention / register |
| jei-dev | JEI / recipe display / ghost drag |
| patchouli-dev | Patchouli / guide book / page |
| patchouli-writer | **★Patchouli 条目生成★** 输入物品中文描述→自动生成双语 entry JSON / 只需文本+@分类名 / 自动查注册名 / 问配方 |
| prototype-charge-jump | charge jump / jump height |
| prototype-flight-mixin | elytra flight / Mixin / velocity |
| openspec-propose | explore / design / spec |
| openspec-apply-change | implement / build / fix task |
| grill-me | discussion / plan / design |
| grill-with-docs | new term / concept / ADR |
| closeout-feature | 归档 / 记录 / 完结 / 功能冻结 |

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

### 编码安全（📛 硬性阻断，不可绕过）

**IF 对含中文/非 ASCII 文件做任何全局文本替换 THEN 必须使用 Node.js 读写，禁止通过 PowerShell 管道/重定向。**

```bash
# ✅ 正确 — Node.js 二进制安全
node -e "fs.writeFileSync('f', fs.readFileSync('f','utf8').replace(/x/g,'y'), 'utf8')"

# ❌ 禁止 — PowerShell 隐式转码会破坏 UTF-8
# git show cb142cd:file | Set-Content file -Encoding UTF8
# git show cb142cd:file > file
# (Get-Content file) -replace 'x','y' | Set-Content file
```

**IF 修改了含中文的文件（`.java` / `.json` / `.properties` / `.toml` / `.cfg`）THEN 提交前必须先执行编码 roundtrip 校验：**

```bash
node .opencode/scripts/encoding-check.mjs
```

失败时该命令 exit(1)，**禁止任何提交被创建**，必须修复后再试。

**IF 修改了 lang `.json` 文件 THEN 额外人工确认：**
1. `§` 颜色码未被错误转码（检查 `搂` / `鈮` 等典型乱码字符计数为 0）
2. `JSON.parse()` 通过
3. 所有 key 在 `en_us.json` 中有对应翻译值且非空

**修复损坏文件的标准方法：**
```bash
node .opencode/scripts/encoding-check.mjs --fix
```
该命令从 `HEAD` 恢复编码损坏的文件。如果 `HEAD` 版本也损坏则跳过，需从更早的稳定 commit 恢复。

### 运行验证（每次改完代码自动 runClient）

IF auto-commit 包含 `src/` 下的生产代码变更（`.java` 文件） THEN 自动执行 runClient 验证：

1. `Start-Process -NoNewWindow gradlew runClient`
2. `Wait-Process -Timeout 240` 等 240 秒
3. 如果进程退出 → 立即读 `run/logs/latest.log`
4. 如果 240 秒超时 → `Stop-Process -Force` 杀进程 → 读日志
5. 搜索 `ERROR / CRASH / Exception / Caused by / Mixin`
6. 无错误 → 告诉用户"编译启动通过，可以进游戏测试了"
7. 有错误 → 按以下流程处理：
   a. **crash 日志自动快照**：`Copy-Item run/logs/latest.log "run/logs/crash-snapshot/$(Get-Date -Format 'yyyyMMdd-HHmmss')-$(Select-String -Pattern 'Caused by|at (com\.GoetyMech|net\.minecraft)' run/logs/latest.log | Select-Object -First 1 | ForEach-Object { $_ -replace '.*\.(\w+)\..*', '$1' }).log"`
   b. **调用 diagnose skill 分析根因**（必须，不得跳过）
   c. **禁止在分析出根因前删除**任何文件（日志 / 存档 / 配置）
   d. 贴出报错摘要 + 根因分析 + 修复建议或无法修复的原因

不触发 runClient 的排除项：仅修改 `docs/`、`AGENTS.md`、`*.json`、`*.yaml`、`*.yml`、`*.toml` 等非代码文件。

### 文档结构规范

创建文档时必须遵循以下目录结构。**禁止在 `docs/items/misc/` 下存放文件**（该目录已废弃）。

**可用类别（category）：**

| 类别 | 用途 | 示例 |
|------|------|------|
| `focus/` | 聚晶（聚焦器）类物品 | `focus/<item>/spec.md` |
| `curio/` | 饰品/首饰类物品 | `curio/<item>/spec.md` |
| `entity/` | 实体（生物/仆从）文档 | `entity/<entity>.md` |
| `reference/` | API 参考、配方系统、模组集成 | `reference/<topic>.md` |
| `design/` | 架构设计、系统模式、原型记录 | `design/<topic>.md` |

**物品/方块/实体文档规则：**
- 每个 item 必须用 `<category>/<item>/spec.md` + `<category>/<item>/timeline.md` 目录格式
- flat `.md` 文件已废弃（仅 `entity/` 和 `reference/` 仍允许 flat 文件）
- 新建时使用脚手架脚本：`node .opencode/scripts/new-item-doc.mjs <category> <item>`
- 新增 category 必须先在本文件注册

### 即时错误记录

IF 调试超过 **15 分钟**未定位、且根因非 trivial THEN 压缩上下文或关闭对话前写入 `docs/incidents/<subdir>/<YYYYMMDD>.<slug>.md`。

**子目录规则：**

| 子目录 | 用途 |
|--------|------|
| `_meta/` | 工作流/工具链/流程类错误 |
| `_mimic/` | 拟态系统相关 |
| `_craft/` | 渲染/建模/原型/物理经验（原 `_skills`） |
| `_prototype/` | 物理原型经验 |
| `<item-reg-name>/` | 绑定到某个注册物品的错误 |

- 文件名去掉 domain 前缀（如 `_meta.20260623.foo.md` → `_meta/20260623.foo.md`）
- 「15 分钟」= 跨对话累计纯调试时间
- 同一根因的多次尝试 = 一个错误，不重复写
- 记录头部含 `status: verified | disputed | superseded`

### 物品文档自动记录

IF 任何代码修改涉及已注册物品/方块/实体 THEN 自动在 `docs/items/<category>/<item>/timeline.md` 追一条记录（格式：改动 / 原因 / 关联）。

触发条件不只是 openspec task 完成，还包括：
- 直接编辑了物品/方块/实体的 Java 源文件
- 修改了注册类（ModItems/ModBlocks/ModEntities）
- 新建了物品/方块/实体的 Java 类文件
- openspec-apply-change 标记 task 完成

**对新物品/方块/实体：** 如果文档尚不存在，先用 `node .opencode/scripts/new-item-doc.mjs <category> <item>` 创建目录结构，再追加 timeline。
openspec-apply-change skill 已集成此步骤（step 6 子步骤）。

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

### 多对话协同约束

IF 开始一个新任务（修改代码） THEN 必须先声索文件：
1. `node .opencode/scripts/task-mgmt.mjs start --desc "<任务描述>" --files "<文件1,文件2>"`
2. 文件路径相对于项目根目录，用逗号分隔

IF 编译报错 THEN：
1. 读取 `run/logs/latest.log`，提取栈追踪中的文件名
2. 对照自己的声索文件：错误涉及其中任何一个？
   - **是** → 修复自己的代码
   - **否** → ⛔ 立即停止，不动任何代码，报告用户：`编译错误不在声索文件范围内，可能是其他对话的任务未完成`

IF 声索文件不在出错栈中 THEN 禁止执行任何 `edit` / `write` 操作（工具不能改代码）。

IF 任务完成 THEN 执行 `node .opencode/scripts/task-mgmt.mjs complete [--id <id>]`。

IF 想编辑一个被其他任务声索的文件 THEN plugin 会警告，此时必须停止。

### 用户说「归档」时做什么

IF 用户说「归档 / 记录一下 / 记下来」THEN 调用 closeout-feature skill 执行 4 步 protocol：
1. timeline 追加
2. 经验提取（写 incidents）
3. incident 索引更新
4. 整理清理

不触发 openspec archive-change（那是管 change git tag 的不同动作）。

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

**提交前必须先执行编码检查（失败则禁止提交）：**
```bash
node .opencode/scripts/encoding-check.mjs
if ($?) { git commit ... }
```

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
