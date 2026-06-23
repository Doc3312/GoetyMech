## Why

玩家需要一种自动施法手段来解放手动操作，同时保持与 Goety 聚晶系统的兼容性。血肉晶匣作为背饰 Curio，内置聚晶存储和独立冷却，实现受击反击/残血保命等自动化策略。

## What Changes

- 新增 Curio 物品「血肉晶匣」(Flesh Casket)，占用 BACK 槽
- 三种触发变体：A 反击型、B 半血型、C 可配阈值型，通过诅咒注入器互转
- 三个等级：基础(1格)/高级(3格)/史诗(5格)，等级独立于触发类型
- 基础版传空法杖(最低效)，高级/史诗传虚拟法杖(最高效)
- 高级/史诗仅限巫妖玩家使用
- 独立冷却系统，不与玩家聚晶袋共享
- 自动目标判定：自施法/反击/召唤/功能四类
- 半魂消耗

## Capabilities

### New Capabilities
- `flesh-casket-item`: 血肉晶匣物品本体，内部聚晶容器
- `auto-cast-trigger`: 三种触发条件的 auto-cast 事件系统
- `auto-cast-target`: 自动目标判定系统
- `independent-cooldown`: 独立冷却系统
- `lich-restriction`: 高级/史诗巫妖限制
- `dummy-wand`: 虚拟法杖用于高级版最大可行性
- `cursed-infuser-conversion`: 诅咒注入器转换配方

### Modified Capabilities

<!-- No existing capabilities are being modified -->

## Impact

- 新增 6+ Java 类
- 新增 Curios BACK 槽位注册
- 新增诅咒注入器配方 JSON
- 新增 ModConfig 配置项
- 新增物品模型/纹理/本地化
