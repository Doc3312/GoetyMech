## Context

GoetyMech 已有成熟胚胎（MatureEmbryoItem）作为多态刷怪物品，可直接右键生成仆从。但缺乏批量召唤机制。Goety 原生提供 TroopFocus（按类型召唤全部）和 CallFocus（按 UUID 拉取单个），均不满足"配置编队→远程消耗→批量生成"的需求。

当前项目结构：
- 方块注册：`ModBlocks` (DeferredRegister)
- 物品注册：`ModItems` (DeferredRegister)
- 容器注册：需要新增 `ModMenuTypes`
- 实体注册：`ModEntities`
- 客户端注册：`ModClientEvents`
- Goety 聚晶基类：`MagicFocus` → 绑定 `ISpell`
- Goety 仆从生命周期：`IOwned` 内置 `limitedLifespan`/`limitedLifeTicks`

## Goals / Non-Goals

**Goals:**
- 点将台方块：三阶（6/12/18 squad slot），GUI 配置，全方向机械动力输入
- 兵团聚晶：绑定点将台 → 远程施法 → 消耗胚胎/灵魂 → 限时生成仆从
- 区块常加载：点将台所在 chunk force-load，三阶跨维度
- 灵魂折扣：二阶 50%，三阶 10%（一阶全额）
- 仆从限时 5 分钟

**Non-Goals:**
- 不涉及合成配方（后续单独加）
- 不涉及新实体 AI
- 不修改 Goety 核心库
- 不跨维度传送已有实体

## Decisions

### 1. Squad Slot 配置方式：胚胎直插

每个 squad slot 固定放 1 个 MatureEmbryo（maxStack=1），slot 数量随阶递增。胚胎的 `EntityTypeId` NBT 决定召唤什么兵种。

- 替代方案（预设编辑器）：UI 复杂，MVP 不需要

### 2. 聚晶灵魂消耗动态化：`defaultSoulCost() = 0`

Goety 的 `MagicFocus.soulCost` 在构造时固定写入，不支持动态。设为 0 后 DarkWand 不做预检和预扣，全部在 `SquadSpell.SpellResult()` 中手动处理。

- tooltip 覆盖显示"每单位消耗 X 灵魂"+折扣说明，而非数字

### 3. 区块加载：TicketType 常加载

方块放置时通过 `ServerChunkCache.addRegionTicket()` 注册常加载 ticket，破坏时移除。三阶额外存储维度 ID，跨维度施法时从 `MinecraftServer.getLevel(dim)` 访问目标维度。

### 4. 仆从限时：复用 IOwned 生命周期

`MinionSpawnHelper.spawnMinion()` 末尾增加 `mob.setHasLifespan(true); mob.setLifespan(6000);`。所有继承 `Owned` 的 Goety 仆从在 `ownedTick()` 中自动递减并消亡。

### 5. 灵魂消耗复用现有配置

每个实体消耗 `ModConfig.MATURE_EMBRYO_SOUL_COST`（默认 100）。总消耗 = 非空 slot 数 × baseCost × tierDiscount。使用 `MinionSpawnHelper.handleSoulCost()` 的扣魂逻辑，但按总价一次性校验。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|---|---|
| Chunk 常加载过多个点将台影响性能 | 设计上预期每玩家 1-2 个点将台，量级可控；可在 config 中添加禁用选项 |
| `defaultSoulCost()=0` 导致 wand tooltip 显示"消耗 0" | 在 `SquadFocus.appendHoverText()` 中覆盖，显示说明文字而非数字 |
| 跨维度施法时目标维度未加载 | 三阶 ticket 确保目标维度区块常加载；一阶二阶仅同维度使用 |
| GUI 动态 slot 数（18 slots + 9 储备）尺寸问题 | 储备仓库固定 9 格置于下方，squad slot 按阶显示 1/2/3 行 × 6 列 |
