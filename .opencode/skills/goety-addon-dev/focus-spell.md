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

## SpellResult 中的聚晶/法杖访问模式

Goety 的 Spell 体系中有三个正交的概念，理解它们的区别是正确开发的前提。

### 三条原则

| # | 原则 | 说明 |
|---|------|------|
| 1 | **附魔读聚晶** | 聚晶上的 Range/Potency/Duration 附魔通过 `WandUtil.enchantedFocus(caster)` + `WandUtil.getRangeLevel(caster)` 等读取 |
| 2 | **增强看法杖** | 法杖类型是否匹配法术流派通过 `rightStaff(staff)` 检查，决定是否解锁额外行为（更高伤害、更多数量、引导模式等） |
| 3 | **冷却/焦点 API 用 `WandUtil.findFocus(caster)`** | 需要直接操作聚晶的 `ItemStack` 时使用，必须做 `isEmpty()` 守卫 |

### 三者的关系：互补而非替代

```
WandUtil.enchantedFocus(caster)  ─── 量变：增强法术强度（数值）
rightStaff(staff)                ─── 质变：改变法术行为（模式）
WandUtil.findFocus(caster)       ─── 获取聚晶 ItemStack（仅特殊场景）
```

它们在 Goety 的 85+ 个 Spell 中**同时出现**，用于不同目的。不存在其中一个是另一个的"替代写法"。

### 各模式的真实使用频率（Goety 源码统计）

| 模式 | 文件数 | 典型用途 |
|------|--------|---------|
| `WandUtil.enchantedFocus(caster)` | **104** | 聚晶附魔→增强数值（准标准） |
| `rightStaff(staff)` | **91** | 法杖类型匹配→解锁增强行为 |
| `WandUtil.findFocus(caster)` | **5** | 直接操作聚晶 ItemStack |
| `IWand.getFocus(staff)` | **0 (Spell内)** | Spell 子类从不直接调用 |

**结论：** `IWand.getFocus(staff)` 是 DarkWand/工具类的底层方法，Spell 子类应使用 `WandUtil` 工具类而非直接调用。

### 正确模板

```java
@Override
public void SpellResult(ServerLevel worldIn, LivingEntity caster,
                        ItemStack staff, SpellStat spellStat) {
    // ── 1. 聚晶附魔加成（标准模式，Goety 104 个 Spell 都这么写）
    int potency = spellStat.getPotency();
    int range = spellStat.getRange();
    if (WandUtil.enchantedFocus(caster)) {
        potency += WandUtil.getPotencyLevel(caster);
        range += WandUtil.getRangeLevel(caster);
    }

    // ── 2. 法杖类型增强（rightStaff = 检查法杖是否匹配法术流派）
    boolean useStaffBonus = rightStaff(staff);
    if (useStaffBonus) {
        potency *= 2;          // 示例：威力翻倍
    }

    // ── 3. 施法逻辑（略）

    // ── 4. 冷却（必须 isEmpty 守卫，自动施法场景下聚晶可能为空）
    ItemStack focus = WandUtil.findFocus(caster);
    if (!focus.isEmpty() && caster instanceof Player player) {
        int cooldown = (int) (this.spellCooldown(caster)
                * ModAttributes.getCooldownDiscount(caster));
        SEHelper.addCooldown(player, focus.getItem(), cooldown);
    }
}
```

### 为什么不用 `IWand.getFocus(staff)` 替代 `WandUtil.findFocus(caster)`

自动施法（FleshCasket 等）传入的 `staff` 是临时创建的 DummyWand，内部无聚晶槽位。此时 `IWand.getFocus(staff)` 返回 `EMPTY`，而 `WandUtil.findFocus(caster)` 还能尝试从玩家手持法杖读取 — 后者有更广的兼容性。这也是 Goety 自身也用 `WandUtil` 而非 `IWand.getFocus` 的原因。

### 常见错误

| 错误写法 | 问题 | 正确写法 |
|---------|------|---------|
| `IWand.getFocus(staff)` 替代 enchantedFocus | 自动施法时聚晶不可达，丢失附魔加成 | `WandUtil.enchantedFocus(caster)` |
| 只调 `rightStaff` 不调 `enchantedFocus` | 丢失了聚晶附魔的数值加成 | 两者都调，各司其职 |
| `SEHelper.addCooldown(player, WandUtil.findFocus(caster).getItem(), cd)` 无守卫 | 聚晶为空时对 `Items.AIR` 上冷却，冷却无效 | 加 `isEmpty()` 守卫，聚晶为空时跳过 |

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
        // 附魔加成 — Goety 标准模式
        int potency = spellStat.getPotency();
        if (WandUtil.enchantedFocus(caster))
            potency += WandUtil.getPotencyLevel(caster);

        // 法杖类型匹配增强 — 所有 Spell 都应加上
        if (rightStaff(staff)) {
            potency *= 2;
        }

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
        if (rightStaff(staff)) {
            potency *= 2;
            duration += 2;
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
