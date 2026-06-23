# Goety 核心 API

## 必须掌握的接口

**法术体系：**
- `api/magic/ISpell.java` — 核心契约：灵魂消耗、施法、冷却、SpellResult
- `api/magic/ISummonSpell.java` — 召唤扩展：SummonDownDuration、commonResult
- `api/magic/IChargingSpell.java` — 蓄力型
- `api/magic/IBreathingSpell.java` — 喷吐型
- `api/magic/IBlockSpell.java` — 方块施法型
- `api/magic/ITouchSpell.java` — 近战型
- `api/magic/SpellType.java` — 法术流派（NECROMANCY/FROST/GEOMANCY 等）

**物品体系：**
- `api/items/magic/IFocus.java` — 聚晶接口：`getSpell()` 将物品映射到法术
- `api/items/magic/IWand.java` — 法杖接口：getSpellType、getFocus、isOnCooldown
- `api/items/magic/ITotem.java` — 图腾/灵魂容器

**召唤物体系：**
- `api/entities/IOwned.java` — 所有者追踪、阵营判定
- `api/entities/ally/IServant.java` — 仆从命令：跟随/守卫/巡逻/停留
- `api/entities/ITrainable.java` — 可训练实体

**玩家状态（Capability）：**
- `common/capabilities/soulenergy/ISoulEnergy.java` — 灵魂能量
- `common/capabilities/lichdom/ILichdom.java` — 巫妖化
- `common/capabilities/misc/IMisc.java` — 杂项状态

## 协议急速查询表

| 需要 | 依赖接口/类 | 路径 |
|------|------------|------|
| 新法术 | `Spell` (继承) 或 `ISpell` (实现) | `common/magic/Spell.java` |
| 新聚晶 | `IFocus` → `MagicFocus` | `common/items/magic/MagicFocus.java` |
| 新召唤物 | `Summoned` (继承) | `common/entities/Summoned.java` |
| 新弹射物 | `SpellHurtingProjectile` (继承) | `common/entities/projectiles/SpellHurtingProjectile.java` |
| 新仪式 | `IRitualType` → `RitualType.addRitualType()` | `api/ritual/` |
| 读灵魂能量 | `SoulUtil.getSoulEnergy(player)` | `utils/SoulUtil.java` |
| 驯服仆从 | `MobUtil.summonTame(mob, player)` | `utils/MobUtil.java` |

## SpellStat 属性

| 属性 | Getter | 含义 |
|------|--------|------|
| potency | `getPotency()` | 威力/强度 |
| duration | `getDuration()` | 持续时间 |
| range | `getRange()` | 射程 |
| radius | `getRadius()` | AOE 半径 |
| burning | `getBurning()` | 火焰附加 |
| velocity | `getVelocity()` | 弹射物初速 |

---

# Focus 开发完整指南

## 四层架构

```
Layer 1: 物品层 (Focus) — IFocus → MagicFocus → 自定义Focus
                         ↕
Layer 2: 法术逻辑层 (Spell) — ISpell → Spell(基类) / SummonSpell(基类)
Layer 3: 弹射物实体 (可选) — SpellHurtingProjectile (继承)
Layer 4: 召唤物实体 (可选) — Summoned (继承)
```

## Focus 物品注册

```java
// 方式一：简单 Focus（直接绑定 Spell）
public static final RegistryObject<Item> MY_FOCUS = ITEMS.register("my_focus",
    () -> new MagicFocus(new MySpell()));

// 方式二：复杂 Focus（继承 MagicFocus）
public class MySpecialFocus extends MagicFocus {
    public MySpecialFocus() { super(new MySpell()); }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) { /* 潜行右键逻辑 */ }
        return super.use(level, player, hand);
    }
}
```

## 攻击型 Spell 实现

```java
public class MyAttackSpell extends Spell {
    @Override public int defaultSoulCost()       { return SpellConfig.MyCost.get(); }
    @Override public int defaultCastDuration()   { return SpellConfig.MyDuration.get(); }
    @Override public int defaultSpellCooldown()  { return SpellConfig.MyCoolDown.get(); }
    @Override public SpellType getSpellType()    { return SpellType.NECROMANCY; }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster,
                           ItemStack staff, SpellStat spellStat) {
        int potency = spellStat.getPotency();
        if (WandUtil.enchantedFocus(caster))
            potency += WandUtil.getPotencyLevel(caster);

        // 方式A：发射弹射物
        Vec3 look = caster.getViewVector(1.0F);
        MyProjectile p = new MyProjectile(caster.getX() + look.x/2,
            caster.getEyeY()-0.2, caster.getZ() + look.z/2,
            look.x, look.y, look.z, worldIn);
        p.setOwner(caster);
        p.setExtraDamage(potency);
        worldIn.addFreshEntity(p);

        // 方式B：AOE 范围伤害
        // MobUtil.targetedEntities(...)

        this.playSound(worldIn, caster, SoundEvents.GENERIC_EXPLODE);
    }
}
```

## 召唤型 Spell 实现

```java
public class MySummonSpell extends SummonSpell {
    @Override public int defaultSoulCost()         { return SpellConfig.MyCost.get(); }
    @Override public int defaultCastDuration()     { return SpellConfig.MyDuration.get(); }
    @Override public int defaultSpellCooldown()    { return SpellConfig.MyCoolDown.get(); }
    @Override public int SummonDownDuration()      { return SpellConfig.MySummonDown.get(); }
    @Override public SpellType getSpellType()      { return SpellType.NECROMANCY; }

    @Override public Predicate<LivingEntity> summonPredicate() {
        return e -> e instanceof MySummonedEntity;
    }
    @Override public int summonLimit() { return SpellConfig.MyLimit.get(); }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster,
                           ItemStack staff, SpellStat spellStat) {
        this.commonResult(worldIn, caster);
        int potency = spellStat.getPotency();
        int duration = spellStat.getDuration();
        if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getPotencyLevel(caster);
            duration += WandUtil.getLevels(ModEnchantments.DURATION.get(), caster);
        }
        if (!isShifting(caster)) {
            MySummonedEntity summoned = new MySummonedEntity(ModEntityType.MY.get(), worldIn);
            summoned.setTrueOwner(caster);
            summoned.moveTo(BlockFinder.SummonRadius(caster.blockPosition(), summoned, worldIn), 0, 0);
            MobUtil.moveDownToGround(summoned);
            summoned.setLimitedLife(MobUtil.getSummonLifespan(worldIn) * duration);
            summoned.finalizeSpawn(worldIn,
                caster.level.getCurrentDifficultyAt(caster.blockPosition()),
                MobSpawnType.MOB_SUMMONED, null, null);
            this.buffSummon(caster, summoned, potency);
            this.SummonSap(caster, summoned);
            this.setTarget(caster, summoned);
            if (worldIn.addFreshEntity(summoned)) this.uponSummon(worldIn, caster, staff, summoned);
            this.summonAdvancement(caster, summoned);
            this.SummonDown(caster);
            this.playSound(worldIn, caster, ModSounds.SUMMON_SPELL.get());
        }
    }
}
```

## 弹射物实体

```java
public class MyProjectile extends SpellHurtingProjectile {
    public MyProjectile(EntityType<? extends SpellHurtingProjectile> type, Level level) {
        super(type, level);
    }
    public MyProjectile(double x, double y, double z,
                       double vx, double vy, double vz, Level level) {
        super(ModEntityType.MY_PROJECTILE.get(), level);
        this.setPos(x, y, z);
        this.setDeltaMovement(vx, vy, vz);
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (target != owner) target.hurt(damageSources().mobProjectile(this, (LivingEntity)owner), damage);
    }
}
```

## 召唤实体

```java
public class MySummonedEntity extends Summoned {
    public MySummonedEntity(EntityType<? extends Summoned> type, Level level) {
        super(type, level);
    }
    // Summoned 已实现 IOwned + IServant 全部方法
}
```

## 实现步骤清单

1. 构思法术效果 → 确定 SpellType
2. 创建 Spell：继承 `Spell` 或 `SummonSpell` → 实现 `SpellResult()`
3. 创建弹射物实体（可选）：继承 `SpellHurtingProjectile` → 注册
4. 创建召唤实体（可选）：继承 `Summoned` → 注册
5. 注册 Focus 物品：ModItems 中用 `ITEMS.register` 绑定 Spell
6. 添加配置项：在 SpellConfig 中管理法术数值
7. 添加语言文件：en_us.json + zh_cn.json
8. 添加纹理：`assets/<modid>/textures/item/`
9. 测试：灵魂消耗、冷却、召唤上限、附魔加成
