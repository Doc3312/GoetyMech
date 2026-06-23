## Context

当前代码库经过代码审查发现 6 项架构偏离。均为独立、低风险修复，不涉及跨模块架构变更。

## Goals / Non-Goals

**Goals:**
- 消除死代码和空目录
- 消除两个胚胎物品间的重复射线追踪逻辑
- 将 `SquadChunkTickets` 移至正确的 `common/utils/` 包
- 将网络通道定义从主类提取为独立类
- 更新架构 skill 文档同步点将台系统

**Non-Goals:**
- 不改变游戏内功能行为
- 不修改注册逻辑或业务逻辑
- 不引入新依赖

## Decisions

1. **射线追踪提取策略**：在 `MinionSpawnHelper` 中新增 `findSpawnPosition(Player, double)` 静态方法，替代两个胚胎物品中重复的 `use()` 逻辑。方法返回 `Vec3` 生成坐标。
2. **网络通道提取**：在 `common/net/` 下新建 `ModNetwork.java`，从 `GoetyPunkMod.java` 提取 `SimpleChannel` 定义和注册逻辑。
3. **`SquadChunkTickets` 迁移**：保留原类不变，仅在 `common/utils/` 下创建引用类。受限于 Java 包名惯例，直接移动文件并更新所有 import。
4. **skill 文档更新**：直接编辑 `.opencode/skills/goetypunk-architecture/SKILL.md`，新增点将台系统章节，更新包结构树。

## Risks / Trade-offs

- [低] 移动 `SquadChunkTickets` 可能丢失 git 历史跟踪 — 接受，仅 9 行工具类
- [低] `SculkSpikeRenderer.java` 被删除后如有外部引用会编译失败 — 已 grep 确认无引用
- [低] skill 文档手动编辑可能有格式偏差 — 编辑后验证 markdown 结构
