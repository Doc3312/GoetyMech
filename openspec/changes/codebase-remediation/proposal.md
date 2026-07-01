## Why

本次代码审查发现 6 项架构偏离，包括死代码、YAGNI 违规、代码重复和 skill 文档过时。需要系统化修复以保持项目架构整洁，防止技术债务累积。

## What Changes

- **移除死代码**：删除未注册的 `client/entity/SculkSpikeRenderer.java`
- **移除空目录**：删除无文件的 `com/GoetyMech/addon/event/` 目录
- **提取重复逻辑**：将两个胚胎物品中重复的射线追踪定位逻辑提取到 `MinionSpawnHelper`
- **移动工具类**：将 `SquadChunkTickets` 从 `common/block/entity/` 移至 `common/utils/`
- **清理主类内联网络**：将网络通道定义从 `GoetyMechMod.java` 提取为独立类
- **更新架构 skill**：同步 `GoetyMech-architecture` skill 文档，录入点将台子系统

## Capabilities

### New Capabilities
- `codebase-cleanup`: 移除死代码和空目录，重构重复逻辑
- `skill-docs-sync`: 将点将台系统、Menu/GUI/网络包结构补录到架构 skill

### Modified Capabilities
- （无现有 spec 被修改）

## Impact

- `client/entity/SculkSpikeRenderer.java` — 删除
- `com/GoetyMech/addon/event/` — 删除整个空目录
- `common/item/TwistedEmbryoItem.java` — 提取射线追踪逻辑
- `common/item/MatureEmbryoItem.java` — 提取射线追踪逻辑
- `common/utils/MinionSpawnHelper.java` — 新增 `findSpawnPosition()` 方法
- `common/block/entity/SquadChunkTickets.java` — 移动到 `common/utils/`
- `common/net/` — 保持现有位置，由 skill 文档记录
- `.opencode/skills/GoetyMech-architecture/SKILL.md` — 更新
