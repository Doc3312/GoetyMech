# 灵魂统御聚晶 (Focus of Soul Dominion)

## 效果

施法后连接 100 格范围内所有属于玩家的仆从（`IOwned`），持续 90 秒。
持续期间，玩家受到的所有伤害（`BYPASSES_EFFECTS` / `BYPASSES_INVULNERABILITY` 除外）均分给所有链接的仆从，玩家无伤。

## 机制

| 项目 | 值 |
|------|-----|
| SpellType | `NECROMANCY` |
| 灵魂消耗 | 2000 |
| 冷却 | 2400 ticks (120s) |
| 基础持续时间 | 90s |
| 基础范围 | 100 blocks |
| 提前取消 | 不支持 |
| 仆从全灭 | 效果自动结束 |

## 附魔

| 附魔 | 效果 |
|------|------|
| DURATION | +10s/级 |
| RANGE | +10 blocks/级 |

## 实现

- Capability: `ISoulDominion` (Player 附加)
- 事件: `LivingAttackEvent @HIGHEST` 取消伤害 + 均分给仆从
- `PlayerTickEvent` 检查过期和仆从全灭
- 客户端: `RenderLevelStageEvent` 红色锁链渲染
