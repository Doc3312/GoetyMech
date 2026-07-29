# 实用工具方法速查

| 方法 | 类 | 用途 | SpellResult内使用 |
|------|----|------|-------------------|
| `MobUtil.summonTame(entity, player)` | `MobUtil` | 驯服仆从（必用） | 召唤时 |
| `MobUtil.moveDownToGround(entity)` | `MobUtil` | 实体落地 | 召唤时 |
| `MobUtil.getSummonLifespan(level)` | `MobUtil` | 召唤寿命基数 | 召唤时 |
| `MobUtil.getOwner(entity)` | `MobUtil` | 获取所有者 | 判定时 |
| `WandUtil.enchantedFocus(caster)` | `WandUtil` | 检查聚晶是否附魔 | ✅ 附魔加成 |
| `WandUtil.getPotencyLevel(caster)` | `WandUtil` | 获取威力附魔等级 | ✅ 附魔加成 |
| `WandUtil.getRangeLevel(caster)` | `WandUtil` | 获取范围附魔等级 | ✅ 附魔加成 |
| `WandUtil.findFocus(caster)` | `WandUtil` | 获取聚晶 ItemStack | ⚠️ 冷却/焦点API时用，必须先 `.isEmpty()` 守卫 |
| `rightStaff(staff)` | `Spell`(继承) | 法杖类型是否匹配法术流派 | ✅ 解锁增强行为 |
| `BlockFinder.SummonRadius(pos, entity, level)` | `BlockFinder` | 计算出生位置 | 召唤时 |
| `SoulUtil.getSoulEnergy(player)` | `SoulUtil` | 读取灵魂能量 | 条件检查时 |
| `CuriosFinder.hasCurio(caster, item)` | `CuriosFinder` | 检查饰品 | 条件检查时 |
| `SEHelper.addCooldown(player, item, cd)` | `SEHelper` | 设置冷却 | ⚠️ 必须传 `focus.getItem()`, 且 focus 不为空 |
| `SEHelper.decreaseSouls(player, cost)` | `SEHelper` | 扣减灵魂 | ⚠️ 如果 spell 不走 DarkWand 自动扣减 |
| `ServerParticleUtil.addParticlesAroundMiddleSelf(...)` | `ServerParticleUtil` | 粒子效果 | 特效时 |
| `MobUtil.targetedEntities(...)` | `MobUtil` | 获取目标列表 | AOE 时 |

## SpellResult 内核心三件套（所有 Spell 的标配）

```java
@Override
public void SpellResult(ServerLevel worldIn, LivingEntity caster,
                        ItemStack staff, SpellStat spellStat) {
    // 1. 基础数值 + 附魔加成
    int potency = spellStat.getPotency();
    if (WandUtil.enchantedFocus(caster)) {
        potency += WandUtil.getPotencyLevel(caster);
    }

    // 2. 法杖类型增强
    if (rightStaff(staff)) {
        potency *= 2;
    }

    // 3. 冷却（带守卫）
    ItemStack focus = WandUtil.findFocus(caster);
    if (!focus.isEmpty() && caster instanceof Player player) {
        SEHelper.addCooldown(player, focus.getItem(), this.spellCooldown(caster));
    }
}
```
