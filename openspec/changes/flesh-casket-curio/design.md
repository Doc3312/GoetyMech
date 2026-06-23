## Context

Goety 已有聚晶(IFocus) + 法术(ISpell) + 法杖(IWand) 体系，以及 FocusBag/FocusPack 容器 Curio 物品。本项目已有 Curios 集成模式和多个自定义聚晶。

血肉晶匣需要：内部聚晶容器 + 自动施法事件 + 独立冷却 + 目标判定 + 等级/变体系统。

## Goals / Non-Goals

**Goals:**
- 可佩戴在 Curios BACK 槽的饰品，内部存储 1/3/5 个聚晶
- 受击/半血/阈值三种自动触发模式，触发型独立物品，诅咒注入器互转
- 独立冷却不与聚晶袋共享
- 基础版最小可行性（传 EMPTY staff），高级/史诗版最大可行性（传虚拟法杖）
- 高级/史诗仅巫妖可用
- 向左遍历容器，找首个可用聚晶执行

**Non-Goals:**
- 不修改 Goety 本体代码
- 不修改已有聚晶/法术的 SpellResult
- 不支持 GUI 配置（纯事件驱动）

## Decisions

### D1: 变体实现方式 — 同一物品类 + 不同注册名

三个变体（A/B/C）注册为三个不同的物品 ID（`flesh_casket_a` / `flesh_casket_b` / `flesh_casket_c`），共享同一个 `FleshCasketItem` 类，构造时传入变体类型枚举。诅咒注入器在变体之间转换（保持 NBT 中的等级和存量）。

**替代方案**: 单一物品 + NBT 切换。不采用因为诅咒注入器配方需要明确输入/输出物品 ID，同一物品无法表达转换。

### D2: 等级实现 — 同一物品 + NBT tier 字段

不注册多个等级物品 ID。所有等级用同一物品 ID，NBT 存 `tier`（1/2/3 对应基础/高级/史诗）。升级通过特定配方改变 NBT。

**理由**: 聚晶存储在 NBT 中，升级不丢失存量。

### D3: 最大可行性 — 虚拟法杖 AutoCastDummyWand

注册一个隐藏的 `AutoCastDummyWand implements IWand`，`getSpellTypes()` 返回所有 11 个 `SpellType.values()`。高级/史诗版自动施法时用此物品构造的 ItemStack 作为 staff 参数传入 `SpellResult()`。

**替代方案**: 扫描玩家背包找 IWand。不采用因为不一定能找到，且找到的法杖类型可能不匹配。

### D4: 目标判定 — TargetingType 枚举 + Item 映射表

```
enum TargetingType { SELF, RETALIATORY, SUMMON, UTILITY }
```

初始化时注册 Goety 所有已知聚晶的映射。运行时通过 `focusMap.get(focusItem)` 查询。

**理由**: 比 instanceof/spellType 更精确，新增聚焦只需加一行映射。

### D5: 独立冷却 — NBT 存储 + PlayerTickEvent 递减

在 FleshCasketItem 的 NBT 中存 `Map<String, Integer>`（key = 聚焦物品注册名，value = 剩余 tick）。`PlayerTickEvent` 每 tick 遍历玩家 Curios 中血肉晶匣的冷却表并递减。

**理由**: 不走 SEHelper，彻底与 Goety 冷却解耦。

### D6: 半魂消耗 — 自定义扣魂逻辑

`spell.soulCost(caster, staff) / 2` → `SEHelper.decreaseSouls(player, cost)`。

### D7: 持续施法转瞬发 — 直接调用 SpellResult

跳过 `startSpell()` / `useSpell()` / `stopSpell()` 生命周期，只调用 `SpellResult()`。对于原本需要蓄力的法术，在 auto-cast 场景下直接产生效果。

## Risks / Trade-offs

- **[Risk] staff.is(NAMELESS_STAFF) 不匹配**: 虚拟法杖无法通过硬性物品比较。→ **Mitigation**: 此类检查通常是额外增强，不影响核心功能。
- **[Risk] 法术附魔加成丢失**: 基础版传 EMPTY，无 enchantedFocus 加成。→ 此为设计意图（便利换强度）。
- **[Risk] 自动施法递归伤害**: A 型受击触发攻击法术可能击杀敌人，敌人死亡不产生额外事件。但若法术造成 AoE 伤害误伤友军可能触发连锁。→ **Mitigation**: 每条施法设最小间隔 (5 tick)，和独立冷却共同约束。
- **[Trade-off] 无法支持有 GUI/交互的法术**: CraftingSpell 和 EnderChestSpell 需要打开 GUI，自动施法无法模拟 UI 交互。
