## Why

成熟胚胎目前只能通过手持右键单次召唤，无法实现批量编队召唤。玩家需要一种方式：预设小队阵容 → 远程一键召唤 → 胚胎消耗 + 灵魂折扣，实现从"单兵作战"到"兵团指挥"的玩法升级。

## What Changes

- **新增方块** `squad_barracks`（点将台）：可升阶（苍白金属→黑暗金属→下界合金锭），升阶扩编队容量（6→12→18 人），消耗成熟胚胎召唤对应仆从
- **新增聚晶** `squad_focus`（兵团聚晶）：右键点将台绑定，远程使用消耗胚胎+灵魂，在玩家周围圆形生成编队
- **新增法术** `SquadSpell`：读取绑定方块 → 计算动态灵魂消耗（含等级折扣）→ 消耗胚胎 → 限时仆从（5分钟）
- **Chunk 常加载**：点将台所在区块常加载，支持跨维度操作（三阶）
- **机械动力兼容**：方块暴露全方向 IItemHandler，允许漏斗输送胚胎

## Capabilities

### New Capabilities
- `squad-barracks`: 点将台方块，三阶升、GUI 配置、胚胎存储、全方向机械动力输入
- `squad-focus`: 兵团聚晶，绑定点将台、缓存配置、远程召唤、动态灵魂消耗
- `timed-servants`: 召唤出的仆从 5 分钟限时（复用 Goety IOwned 生命周期系统）
- `chunk-loading`: 点将台区块常加载，三阶跨维度可操作

### Modified Capabilities

（无现有 specs 被修改）

## Impact

| 范围 | 影响 |
|---|---|
| 新增文件 | 6 个：SquadBarracksBlock, SquadBarracksBE, SquadBarracksMenu, SquadBarracksScreen, SquadFocus, SquadSpell |
| 修改文件 | 4 个：ModBlocks, ModItems, ModConfig, ModClientEvents |
| 新增注册 | Block + BE + MenuType + Item（聚晶+BlockItem） |
| 配置项 | 召唤消耗、各阶折扣率、冷却时间、寿命时长 |
