## 1. 死代码 & 空目录清理

- [x] 1.1 删除 `src/main/java/com/goetypunk/addon/client/entity/SculkSpikeRenderer.java`
- [x] 1.2 删除空目录 `src/main/java/com/goetypunk/addon/event/`（以及空了的 `client/entity/`）

## 2. 提取重复射线追踪逻辑

- [x] 2.1 在 `MinionSpawnHelper.java` 中新增 `findSpawnPosition(Player, double)` 静态方法
- [x] 2.2 重构 `TwistedEmbryoItem.use()` 调用提取的方法
- [x] 2.3 重构 `MatureEmbryoItem.use()` 调用提取的方法

## 3. 移动 SquadChunkTickets 到 common/utils/

- [x] 3.1 移动 `SquadChunkTickets.java` 到 `common/utils/` 并更新 package 声明
- [x] 3.2 更新 `SquadBarracksBlockEntity.java` 中的 import

## 4. 提取网络通道到独立类

- [x] 4.1 在 `common/net/` 下新建 `ModNetwork.java`，从 `GoetyPunkMod.java` 提取 `SimpleChannel`
- [x] 4.2 更新 `GoetyPunkMod.java` 引用 `ModNetwork.init()`
- [x] 4.3 更新 `SquadBarracksScreen.java` 中的 `GoetyPunkMod.NETWORK` 引用为 `ModNetwork.CHANNEL`

## 5. 更新架构 skill 文档

- [x] 5.1 在 SKILL.md 包结构树中新增 `common/menu/`, `common/net/`, `client/gui/`, `client/render/`
- [x] 5.2 新增「点将台系统」设计决策章节（4.5）
- [x] 5.3 修复 client 渲染器路径，更新分层约定表

## 6. 验证

- [x] 6.1 运行 `gradlew build` 确认编译通过
- [x] 6.2 grep 确认 `SculkSpikeRenderer` 无遗留引用
