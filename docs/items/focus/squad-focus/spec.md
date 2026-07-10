# 兵团聚晶 — GoetyMech

## 身份

- 类型：focus
- 注册类：ModItems.SQUAD_FOCUS
- Java 类：SquadFocus (extends MagicFocus)
- 首次创建：2026-06-14

## 设计意图

远程绑定「点将台」/「魂灵丰碑」后，批量召唤编队仆从。从"单兵作战"升级到"兵团指挥"。

## 双绑定模式

| 模式 | bound_type | 绑定对象 | 召唤方式 | 冷却 |
|------|-----------|---------|---------|------|
| 点将台 | `barracks` | SquadBarracksBlockEntity | 消耗胚胎+灵魂 | 10s |
| 丰碑 | `monument` | SoulMonumentBlockEntity | 读 .dat 生成限时副本 | 5min |

### 丰碑绑定特性

- 追踪存活仆从：全存活→传送身边；部分死→补召（`monument_slot` 索引追踪）
- shift+右键 dismiss：`DarkWandUseMixin` 在 `cannotCast()` 前截获，不触发 SpellResult/冷却
- 三防掉落：`LivingHurtEvent`（致死拦截）+ `LivingDropsEvent`（兜底）→ 任何死亡方式均无掉落
- 丰碑旁 64 格内信标 buff 传播给召唤仆从（`BeaconHelper`）

### 关键文件

- `SquadFocus.java` — 聚晶物品
- `SquadSpell.java` — Spell 实现（handleMonumentSummon / spawnSquad）
- `SoulMonumentEvents.java` — LivingHurtEvent + LivingDropsEvent 拦截
- `BeaconHelper.java` — 信标效果扫描
- `DarkWandUseMixin.java` — shift+dismiss 法杖注入

