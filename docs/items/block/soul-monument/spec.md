# 魂灵丰碑 (Soul Monument)

三阶方块，存储实体完整 NBT 快照，通过 TaglockKit 标记 + 村民献祭来录入仆从，通过独立 .dat 文件持久化，可与兵团聚晶绑定召唤限时仆从。

## 等级

| 等级 | 方块类 | 容量 |
|------|--------|------|
| I | Tier1SoulMonumentBlock | 4 |
| II | Tier2SoulMonumentBlock | 8 |
| III | Tier3SoulMonumentBlock | 12 |

## 录入流程

1. TaglockKit 标记已有驯服仆从
2. 右键丰碑开始（300s 超时，黑烟/灵魂粒子/WITHER 音效）
3. 村民在范围（10 blocks）被杀触发 DISMISSED 处决
4. 采集 NBT 写入 `world/data/goetymech/monument/<UUID>.dat`
5. 仆从不可复活（`goetymech.recorded` + DISMISSED）

## 召唤

兵团聚晶绑定丰碑后可召唤限时仆从（复制实体，不消耗数据）。召唤仆从有 `monument_summoned` 标记：
- 所有死亡方式均被 `LivingHurtEvent`（致死）+ `LivingDropsEvent`（兜底）拦截，**绝对无掉落物**
- 追踪存活仆从（`monument_slot` 索引）：全存活→传送身边；部分死→补召

## 关键文件

- `SoulMonumentBlock.java` — 方块基类
- `SoulMonumentBlockEntity.java` — BE 状态机 + 处决逻辑
- `SoulMonumentDataManager.java` — .dat 文件读写
- `SoulMonumentEvents.java` — TaglockKit 交互 + 村民死亡检测
- `SquadFocus.java` — bound_type 字段 `monument`
- `SquadSpell.java#handleMonumentSummon()` — 召唤分派

## 注册

- `ModBlocks`: TIER1/2/3_SOUL_MONUMENT + SOUL_MONUMENT_ENTITY
- `ModItems`: TIER1/2/3_SOUL_MONUMENT (SoulMonumentBlockItem, fireResistant)
- `ModCreativeTab`: 三个丰碑

## 录入校验规则（v1 — 2026-07-28）

TaglockKit 右键丰碑时，三层校验链：

```
Event 层 onRightClickBlock
  → 同维度按 UUID 查找实体
  → 校验 IOwned + 玩家所有 + 存活
    → BE 层 tryStartRecording（防御性重复校验）
      → BE 层 onVillagerDeath（献祭处决前二次校验所有权）
```

| 检查项 | 条件 | 绕过后果 |
|--------|------|----------|
| 实体实现 `IOwned` | 必须 | 敌对/中立生物被录入 |
| `getOwnerId()` == 玩家 | 必须 | 野生仆从被收编为玩家所有 |
| 实体存活 | 必须 | 无影响（onVillagerDeath 会跳过） |
| 同维度 | 必须（隐式） | 跨维度实体无法参与献祭 |

校验失败统一提示：`§c该生物非仆从`
