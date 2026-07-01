---
name: "goety-addon-dev"
description: "GoetyMech 附属模组开发综合指南。涵盖 Forge 1.20.x 基础、Goety API 接入、聚晶/Focus/Spell 开发、实体/仆从实现、配置与网络。所有涉及代码编写、注册、构建、调试的操作都必须调用本 skill。"
---

# GoetyMech Addon 开发指南

> 前置源码路径：`D:\Libcode\Goety-2-1.20\Goety-2-1.20\`、`D:\Libcode\Create-mc1.20.1-dev\Create-mc1.20.1-dev\`、`D:\Libcode\ending_library-1.20.1\`

## Forge 1.20.x 基础

### mods.toml
```toml
modLoader = "javafml"
loaderVersion = "[47,)"
[[mods]]
modId = "your_mod_id"
displayName = "Your Mod"
```

### 主类
```java
@Mod("your_mod_id")
public class YourMod {
    public static final String MOD_ID = "your_mod_id";
    public YourMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    }
}
```

### 注册表（DeferredRegister）
```java
public static final DeferredRegister<Block> BLOCKS =
    DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
public static final RegistryObject<Block> EXAMPLE_BLOCK =
    BLOCKS.register("example", () -> new Block(BlockBehaviour.Properties.of()));
```
注册顺序（强制）：方块 → 方块实体 → 物品(BlockItem) → 创造标签页

### 事件系统
- **模组事件总线**：生命周期、注册事件
- **Forge 事件总线**：运行时事件

### 端位隔离
- 跨逻辑端通信必须用网络数据包
- 客户端代码隔离在 `client` 包
- 使用 `DistExecutor.safeRunWhenOn(Dist.CLIENT, ...)`

### 常见陷阱
- `Entity.level` 在 1.20.x 是 private 字段，必须用 getter `level()`
- 同样适用于 `Entity.position()` 等方法

### 网络（SimpleImpl）
```java
public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
    new ResourceLocation(MOD_ID, "main"), () -> "1", "1"::equals, "1"::equals);
```

---

## Goety 核心 API

### 法术体系
- `api/magic/ISpell.java` — 核心契约：灵魂消耗、施法、冷却、SpellResult
- `api/magic/ISummonSpell.java` — 召唤扩展：SummonDownDuration、commonResult
- `api/magic/IChargingSpell.java` — 蓄力型
- `api/magic/IBreathingSpell.java` — 喷吐型
- `api/magic/IBlockSpell.java` — 方块施法型
- `api/magic/ITouchSpell.java` — 近战型
- `api/magic/SpellType.java` — 法术流派（NECROMANCY/FROST/GEOMANCY 等）

### 物品体系
- `api/items/magic/IFocus.java` — 聚晶接口：`getSpell()` 将物品映射到法术
- `api/items/magic/IWand.java` — 法杖接口：getSpellType、getFocus、isOnCooldown
- `api/items/magic/ITotem.java` — 图腾/灵魂容器

### 召唤物体系
- `api/entities/IOwned.java` — 所有者追踪、阵营判定
- `api/entities/ally/IServant.java` — 仆从命令：跟随/守卫/巡逻/停留
- `api/entities/ITrainable.java` — 可训练实体

### 协议急速查询表

| 需要 | 依赖接口/类 | 路径 |
|------|------------|------|
| 新法术 | `Spell` (继承) 或 `ISpell` (实现) | `common/magic/Spell.java` |
| 新聚晶 | `IFocus` → `MagicFocus` | `common/items/magic/MagicFocus.java` |
| 新召唤物 | `Summoned` (继承) | `common/entities/Summoned.java` |
| 新弹射物 | `SpellHurtingProjectile` (继承) | `common/entities/projectiles/SpellHurtingProjectile.java` |
| 新仪式 | `IRitualType` → `RitualType.addRitualType()` | `api/ritual/` |
| 读灵魂能量 | `SoulUtil.getSoulEnergy(player)` | `utils/SoulUtil.java` |
| 驯服仆从 | `MobUtil.summonTame(mob, player)` | `utils/MobUtil.java` |

### SpellStat 属性
| 属性 | Getter | 含义 |
|------|--------|------|
| potency | `getPotency()` | 威力/强度 |
| duration | `getDuration()` | 持续时间 |
| range | `getRange()` | 射程 |
| radius | `getRadius()` | AOE 半径 |
| burning | `getBurning()` | 火焰附加 |
| velocity | `getVelocity()` | 弹射物初速 |

---

## Focus 开发完整指南

### 四层架构
```
Layer 1: 物品层 (Focus) — IFocus → MagicFocus → 自定义Focus
Layer 2: 法术逻辑层 (Spell) — ISpell → Spell(基类) / SummonSpell(基类)
Layer 3: 弹射物实体 (可选) — SpellHurtingProjectile (继承)
Layer 4: 召唤物实体 (可选) — Summoned (继承)
```

### Focus 物品注册
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

### 攻击型 Spell 实现
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
        // 发射弹射物或直接伤害...
    }
}
```

---

## 实用工具方法速查

| 方法 | 类 | 用途 |
|------|----|------|
| `MobUtil.summonTame(entity, player)` | `MobUtil` | 驯服仆从（必用） |
| `MobUtil.moveDownToGround(entity)` | `MobUtil` | 实体落地 |
| `MobUtil.getSummonLifespan(level)` | `MobUtil` | 召唤寿命基数 |
| `MobUtil.getOwner(entity)` | `MobUtil` | 获取所有者 |
| `WandUtil.enchantedFocus(caster)` | `WandUtil` | 检查附魔 Focus |
| `WandUtil.getPotencyLevel(caster)` | `WandUtil` | 获取威力等级 |
| `BlockFinder.SummonRadius(pos, entity, level)` | `BlockFinder` | 计算出生位置 |
| `SoulUtil.getSoulEnergy(player)` | `SoulUtil` | 读取灵魂能量 |
| `CuriosFinder.hasCurio(caster, item)` | `CuriosFinder` | 检查饰品 |
| `ServerParticleUtil.addParticlesAroundMiddleSelf(...)` | `ServerParticleUtil` | 粒子效果 |
| `MobUtil.targetedEntities(...)` | `MobUtil` | 获取目标列表 |

---

## Warden（坚守者）开发实战经验

### Warden Brain 活动优先级
| 优先级 | 活动 | 说明 |
|--------|------|------|
| 0 | EMERGE | 出土动画 |
| 0 | DIG | 遁地 |
| 1 | ROAR | 怒吼 |
| 2 | FIGHT | 战斗 |
| 3 | INVESTIGATE | 调查 |
| 4 | SNIFF | 嗅探 |
| 6 | IDLE | 空闲 |

**核心陷阱**：DIG 和 EMERGE 优先级相同且都高于 FIGHT。即使设置 ATTACK_TARGET，如果 Warden 有遁地条件，Brain 会优先 DIG。

**正确做法**：召唤时设置 DIG_COOLDOWN 禁止遁地：
```java
warden.getBrain().setMemoryWithExpiry(
    MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200);
```

### 激怒 Warden 的方式
**推荐方案**：使用 3 参数重载直接指定愤怒值（Goety 官方做法）：
```java
// 100 = AngerLevel.ANGRY.getMinimumAnger() (80) + 20
// false = 不触发 Sculk Shrieker 连锁召唤
warden.increaseAngerAt(target, 100, false);
```

3 参数重载签名（vanilla `Warden` 类）：
```java
public void increaseAngerAt(@Nullable LivingEntity target, int angerIncrease, boolean listen)
```
- `target` — 愤怒目标
- `angerIncrease` — 直接指定愤怒增加值
- `listen` — `true` 时触发附近 Sculk Shrieker 振动监听链（可能再召 Warden），`false` 静默注入

### Warden 目标死后转火（LivingDeathEvent 重定向）
1. Warden 生成时打标签（PersistentData）
2. LivingDeathEvent 中转火（仅击杀者自身转火）
3. 法术执行期目标意外死亡补救（SpellResult 中 fallback）

**规则**：
- 仅亲手击杀的 Warden 获得转火（各自独立）
- 避免使用 `setAttackTarget()` 和 Brain 记忆直接操作（不稳定）
- 仅依赖 `increaseAngerAt()` 三参数重载
- `false` 禁止 Sculk Shrieker 连锁召唤