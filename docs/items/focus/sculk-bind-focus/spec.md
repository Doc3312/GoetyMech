# sculk_bind — 幽匿束缚聚晶

- **注册名**: `goetymech:sculk_bind_focus`
- **法术**: `SculkBindSpell`
- **SpellType**: `VOID`
- **灵魂消耗**: `ModConfig.SCULK_BIND_COST`

## 功能

施法后射线锁敌，将目标定身在原地并召唤原版 Warden。
定身由 `SculkSpikeEntity` 每 tick 强制归位实现，Warden 通过 `GameEvent.SHRIEK` 振动链自然索敌攻击目标。

## 可接受附魔

| 附魔 | 效果 | 公式 |
|------|------|------|
| POTENCY（威力） | +1 Warden / 级 | `baseCount + potencyLevel + staffBonus` |
| RANGE（范围） | 锁敌距离 | `baseRange + (lvl × 2)` 格 |
| DURATION（持续时间） | 定身时长 | `baseLock + (lvl + 1)` 秒 |

## 配置项

- `SCULK_BIND_COST` — 灵魂消耗
- `SCULK_BIND_RANGE` — 基础锁敌距离
- `SCULK_BIND_LOCK_DURATION` — 基础定身时长
- `SCULK_BIND_WARDEN_COUNT` — 基础 Warden 数量
- `SCULK_BIND_SUCCESS_COOLDOWN` — 成功施法冷却
- `SCULK_BIND_MISS_COOLDOWN` — 未命中冷却
