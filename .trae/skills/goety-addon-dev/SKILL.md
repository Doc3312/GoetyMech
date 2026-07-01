---
name: "goety-addon-dev"
description: "GoetyMech 闄勫睘妯＄粍寮€鍙戠患鍚堟寚鍗椼€傛兜鐩?Forge 1.20.x 鍩虹銆丟oety API 鎺ュ叆銆佽仛鏅?Focus/Spell 寮€鍙戙€佸疄浣?浠嗕粠瀹炵幇銆侀厤缃笌缃戠粶銆傛墍鏈夋秹鍙婁唬鐮佺紪鍐欍€佹敞鍐屻€佹瀯寤恒€佽皟璇曠殑鎿嶄綔閮藉繀椤昏皟鐢ㄦ湰 skill銆?
---

# GoetyMech Addon 寮€鍙戞寚鍗?
> 鍓嶇疆婧愮爜璺緞锛歚D:\Libcode\Goety-2-1.20\Goety-2-1.20\`銆乣D:\Libcode\Create-mc1.20.1-dev\Create-mc1.20.1-dev\`銆乣D:\Libcode\ending_library-1.20.1\`

## Forge 1.20.x 鍩虹

### mods.toml
```toml
modLoader = "javafml"
loaderVersion = "[47,)"
[[mods]]
modId = "your_mod_id"
displayName = "Your Mod"
```

### 涓荤被
```java
@Mod("your_mod_id")
public class YourMod {
    public static final String MOD_ID = "your_mod_id";
    public YourMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    }
}
```

### 娉ㄥ唽琛紙DeferredRegister锛?```java
public static final DeferredRegister<Block> BLOCKS =
    DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
public static final RegistryObject<Block> EXAMPLE_BLOCK =
    BLOCKS.register("example", () -> new Block(BlockBehaviour.Properties.of()));
```
娉ㄥ唽椤哄簭锛堝己鍒讹級锛氭柟鍧?鈫?鏂瑰潡瀹炰綋 鈫?鐗╁搧(BlockItem) 鈫?鍒涢€犳爣绛鹃〉

### 浜嬩欢绯荤粺
- **妯＄粍浜嬩欢鎬荤嚎**锛氱敓鍛藉懆鏈熴€佹敞鍐屼簨浠?- **Forge 浜嬩欢鎬荤嚎**锛氳繍琛屾椂浜嬩欢

### 绔綅闅旂
- 璺ㄩ€昏緫绔€氫俊蹇呴』鐢ㄧ綉缁滄暟鎹寘
- 瀹㈡埛绔唬鐮侀殧绂诲湪 `client` 鍖?- 浣跨敤 `DistExecutor.safeRunWhenOn(Dist.CLIENT, ...)`

### 甯歌闄烽槺
- `Entity.level` 鍦?1.20.x 鏄?private 瀛楁锛屽繀椤荤敤 getter `level()`
- 鍚屾牱閫傜敤浜?`Entity.position()` 绛夋柟娉?
### 缃戠粶锛圫impleImpl锛?```java
public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
    new ResourceLocation(MOD_ID, "main"), () -> "1", "1"::equals, "1"::equals);
```

---

## Goety 鏍稿績 API

### 娉曟湳浣撶郴
- `api/magic/ISpell.java` 鈥?鏍稿績濂戠害锛氱伒榄傛秷鑰椼€佹柦娉曘€佸喎鍗淬€丼pellResult
- `api/magic/ISummonSpell.java` 鈥?鍙敜鎵╁睍锛歋ummonDownDuration銆乧ommonResult
- `api/magic/IChargingSpell.java` 鈥?钃勫姏鍨?- `api/magic/IBreathingSpell.java` 鈥?鍠峰悙鍨?- `api/magic/IBlockSpell.java` 鈥?鏂瑰潡鏂芥硶鍨?- `api/magic/ITouchSpell.java` 鈥?杩戞垬鍨?- `api/magic/SpellType.java` 鈥?娉曟湳娴佹淳锛圢ECROMANCY/FROST/GEOMANCY 绛夛級

### 鐗╁搧浣撶郴
- `api/items/magic/IFocus.java` 鈥?鑱氭櫠鎺ュ彛锛歚getSpell()` 灏嗙墿鍝佹槧灏勫埌娉曟湳
- `api/items/magic/IWand.java` 鈥?娉曟潠鎺ュ彛锛歡etSpellType銆乬etFocus銆乮sOnCooldown
- `api/items/magic/ITotem.java` 鈥?鍥捐吘/鐏甸瓊瀹瑰櫒

### 鍙敜鐗╀綋绯?- `api/entities/IOwned.java` 鈥?鎵€鏈夎€呰拷韪€侀樀钀ュ垽瀹?- `api/entities/ally/IServant.java` 鈥?浠嗕粠鍛戒护锛氳窡闅?瀹堝崼/宸￠€?鍋滅暀
- `api/entities/ITrainable.java` 鈥?鍙缁冨疄浣?
### 鍗忚鎬ラ€熸煡璇㈣〃

| 闇€瑕?| 渚濊禆鎺ュ彛/绫?| 璺緞 |
|------|------------|------|
| 鏂版硶鏈?| `Spell` (缁ф壙) 鎴?`ISpell` (瀹炵幇) | `common/magic/Spell.java` |
| 鏂拌仛鏅?| `IFocus` 鈫?`MagicFocus` | `common/items/magic/MagicFocus.java` |
| 鏂板彫鍞ょ墿 | `Summoned` (缁ф壙) | `common/entities/Summoned.java` |
| 鏂板脊灏勭墿 | `SpellHurtingProjectile` (缁ф壙) | `common/entities/projectiles/SpellHurtingProjectile.java` |
| 鏂颁华寮?| `IRitualType` 鈫?`RitualType.addRitualType()` | `api/ritual/` |
| 璇荤伒榄傝兘閲?| `SoulUtil.getSoulEnergy(player)` | `utils/SoulUtil.java` |
| 椹湇浠嗕粠 | `MobUtil.summonTame(mob, player)` | `utils/MobUtil.java` |

### SpellStat 灞炴€?| 灞炴€?| Getter | 鍚箟 |
|------|--------|------|
| potency | `getPotency()` | 濞佸姏/寮哄害 |
| duration | `getDuration()` | 鎸佺画鏃堕棿 |
| range | `getRange()` | 灏勭▼ |
| radius | `getRadius()` | AOE 鍗婂緞 |
| burning | `getBurning()` | 鐏劙闄勫姞 |
| velocity | `getVelocity()` | 寮瑰皠鐗╁垵閫?|

---

## Focus 寮€鍙戝畬鏁存寚鍗?
### 鍥涘眰鏋舵瀯
```
Layer 1: 鐗╁搧灞?(Focus) 鈥?IFocus 鈫?MagicFocus 鈫?鑷畾涔塅ocus
Layer 2: 娉曟湳閫昏緫灞?(Spell) 鈥?ISpell 鈫?Spell(鍩虹被) / SummonSpell(鍩虹被)
Layer 3: 寮瑰皠鐗╁疄浣?(鍙€? 鈥?SpellHurtingProjectile (缁ф壙)
Layer 4: 鍙敜鐗╁疄浣?(鍙€? 鈥?Summoned (缁ф壙)
```

### Focus 鐗╁搧娉ㄥ唽
```java
// 鏂瑰紡涓€锛氱畝鍗?Focus锛堢洿鎺ョ粦瀹?Spell锛?public static final RegistryObject<Item> MY_FOCUS = ITEMS.register("my_focus",
    () -> new MagicFocus(new MySpell()));

// 鏂瑰紡浜岋細澶嶆潅 Focus锛堢户鎵?MagicFocus锛?public class MySpecialFocus extends MagicFocus {
    public MySpecialFocus() { super(new MySpell()); }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) { /* 娼滆鍙抽敭閫昏緫 */ }
        return super.use(level, player, hand);
    }
}
```

### 鏀诲嚮鍨?Spell 瀹炵幇
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
        // 鍙戝皠寮瑰皠鐗╂垨鐩存帴浼ゅ...
    }
}
```

---

## 瀹炵敤宸ュ叿鏂规硶閫熸煡

| 鏂规硶 | 绫?| 鐢ㄩ€?|
|------|----|------|
| `MobUtil.summonTame(entity, player)` | `MobUtil` | 椹湇浠嗕粠锛堝繀鐢級 |
| `MobUtil.moveDownToGround(entity)` | `MobUtil` | 瀹炰綋钀藉湴 |
| `MobUtil.getSummonLifespan(level)` | `MobUtil` | 鍙敜瀵垮懡鍩烘暟 |
| `MobUtil.getOwner(entity)` | `MobUtil` | 鑾峰彇鎵€鏈夎€?|
| `WandUtil.enchantedFocus(caster)` | `WandUtil` | 妫€鏌ラ檮榄?Focus |
| `WandUtil.getPotencyLevel(caster)` | `WandUtil` | 鑾峰彇濞佸姏绛夌骇 |
| `BlockFinder.SummonRadius(pos, entity, level)` | `BlockFinder` | 璁＄畻鍑虹敓浣嶇疆 |
| `SoulUtil.getSoulEnergy(player)` | `SoulUtil` | 璇诲彇鐏甸瓊鑳介噺 |
| `CuriosFinder.hasCurio(caster, item)` | `CuriosFinder` | 妫€鏌ラグ鍝?|
| `ServerParticleUtil.addParticlesAroundMiddleSelf(...)` | `ServerParticleUtil` | 绮掑瓙鏁堟灉 |
| `MobUtil.targetedEntities(...)` | `MobUtil` | 鑾峰彇鐩爣鍒楄〃 |

---

## Warden锛堝潥瀹堣€咃級寮€鍙戝疄鎴樼粡楠?
### Warden Brain 娲诲姩浼樺厛绾?| 浼樺厛绾?| 娲诲姩 | 璇存槑 |
|--------|------|------|
| 0 | EMERGE | 鍑哄湡鍔ㄧ敾 |
| 0 | DIG | 閬佸湴 |
| 1 | ROAR | 鎬掑惣 |
| 2 | FIGHT | 鎴樻枟 |
| 3 | INVESTIGATE | 璋冩煡 |
| 4 | SNIFF | 鍡呮帰 |
| 6 | IDLE | 绌洪棽 |

**鏍稿績闄烽槺**锛欴IG 鍜?EMERGE 浼樺厛绾х浉鍚屼笖閮介珮浜?FIGHT銆傚嵆浣胯缃?ATTACK_TARGET锛屽鏋?Warden 鏈夐亖鍦版潯浠讹紝Brain 浼氫紭鍏?DIG銆?
**姝ｇ‘鍋氭硶**锛氬彫鍞ゆ椂璁剧疆 DIG_COOLDOWN 绂佹閬佸湴锛?```java
warden.getBrain().setMemoryWithExpiry(
    MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200);
```

### 婵€鎬?Warden 鐨勬柟寮?**鎺ㄨ崘鏂规**锛氫娇鐢?3 鍙傛暟閲嶈浇鐩存帴鎸囧畾鎰ゆ€掑€硷紙Goety 瀹樻柟鍋氭硶锛夛細
```java
// 100 = AngerLevel.ANGRY.getMinimumAnger() (80) + 20
// false = 涓嶈Е鍙?Sculk Shrieker 杩為攣鍙敜
warden.increaseAngerAt(target, 100, false);
```

3 鍙傛暟閲嶈浇绛惧悕锛坴anilla `Warden` 绫伙級锛?```java
public void increaseAngerAt(@Nullable LivingEntity target, int angerIncrease, boolean listen)
```
- `target` 鈥?鎰ゆ€掔洰鏍?- `angerIncrease` 鈥?鐩存帴鎸囧畾鎰ゆ€掑鍔犲€?- `listen` 鈥?`true` 鏃惰Е鍙戦檮杩?Sculk Shrieker 鎸姩鐩戝惉閾撅紙鍙兘鍐嶅彫 Warden锛夛紝`false` 闈欓粯娉ㄥ叆

### Warden 鐩爣姝诲悗杞伀锛圠ivingDeathEvent 閲嶅畾鍚戯級
1. Warden 鐢熸垚鏃舵墦鏍囩锛圥ersistentData锛?2. LivingDeathEvent 涓浆鐏紙浠呭嚮鏉€鑰呰嚜韬浆鐏級
3. 娉曟湳鎵ц鏈熺洰鏍囨剰澶栨浜¤ˉ鏁戯紙SpellResult 涓?fallback锛?
**瑙勫垯**锛?- 浠呬翰鎵嬪嚮鏉€鐨?Warden 鑾峰緱杞伀锛堝悇鑷嫭绔嬶級
- 閬垮厤浣跨敤 `setAttackTarget()` 鍜?Brain 璁板繂鐩存帴鎿嶄綔锛堜笉绋冲畾锛?- 浠呬緷璧?`increaseAngerAt()` 涓夊弬鏁伴噸杞?- `false` 绂佹 Sculk Shrieker 杩為攣鍙敜