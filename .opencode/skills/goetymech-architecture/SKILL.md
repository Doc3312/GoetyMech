---
name: "GoetyMech-architecture"
description: "package / convention / register"
---

<!-- 鏈妧鑳芥棤璇︾粏瀹炵幇鏂囦欢锛屾墍鏈夊唴瀹瑰潎鍦?SKILL.md 涓?-->

# GoetyMech 椤圭洰鏋舵瀯瑙勮寖

> 鍓嶇疆妯＄粍婧愮爜璺緞瑙?`.opencode/skills/_shared/libcode-paths.md`銆?> 蹇€熸帓闄わ細褰撳墠浠诲姟濡傛灉涓嶆秹鍙婃柊寤虹被銆佸喅瀹氬寘璺緞銆佺悊瑙ｆ敞鍐屾ā寮忔垨璇勪及鏋舵瀯璁捐锛岃璺宠繃鏈?skill銆?
## 閫傜敤鍦烘櫙

- 娣诲姞鏂扮墿鍝?鏂瑰潡/瀹炰綋/鍔熻兘 鈥?纭畾浠ｇ爜鏀惧摢涓寘
- 鏂板缓 Java 绫?鈥?纭畾绫诲悕銆佸寘璺緞銆佺户鎵垮叧绯?- 璇勪及璁捐鍐崇瓥鏄惁绗﹀悎椤圭洰瑙勮寖
- 鐞嗚В褰撳墠浠ｇ爜甯冨眬

---

## 涓€銆侀」鐩爣璇?
| 椤?| 鍊?|
|----|-----|
| **妯＄粍 ID** | `GoetyMech` |
| **涓诲寘** | `com.GoetyMech.addon` |
| **涓诲叆鍙?* | `com.GoetyMech.addon.GoetyMechMod` |
| **MC鐗堟湰** | 1.20.1 / Forge 47.4.0 / Java 17 |
| **鏄犲皠** | Parchment 2023.09.03 |
| **鍓嶇疆** | Curios 5.3.4, Goety 2.x |

---

## 浜屻€佸寘缁撴瀯锛堝綋鍓嶉」鐩疄闄呭竷灞€锛?
```
com.GoetyMech.addon/
鈹溾攢鈹€ GoetyMechMod.java              # 涓诲叆鍙?鈹溾攢鈹€ config/
鈹?  鈹斺攢鈹€ ModConfig.java             # ForgeConfigSpec
鈹溾攢鈹€ init/                          # 娉ㄥ唽灞?鈹?  鈹溾攢鈹€ ModBlocks.java             # 鏂瑰潡 + 鏂瑰潡瀹炰綋
鈹?  鈹溾攢鈹€ ModItems.java              # 鐗╁搧 + Focus
鈹?  鈹溾攢鈹€ ModEntities.java           # 瀹炰綋绫诲瀷
鈹?  鈹溾攢鈹€ ModCreativeTab.java        # 鍒涢€犳爣绛鹃〉
鈹?  鈹斺攢鈹€ ModTags.java               # 鑷畾涔夋爣绛?鈹溾攢鈹€ common/                        # 閫昏緫灞?鈹?  鈹溾攢鈹€ item/
鈹?  鈹?  鈹溾攢鈹€ SculkBindFocus.java
鈹?  鈹?  鈹溾攢鈹€ TwistedEmbryoItem.java
鈹?  鈹?  鈹溾攢鈹€ MatureEmbryoItem.java
鈹?  鈹?  鈹斺攢鈹€ SquadFocus.java
鈹?  鈹溾攢鈹€ block/
鈹?  鈹?  鈹溾攢鈹€ FleshFurnaceBlock.java
鈹?  鈹?  鈹溾攢鈹€ SquadBarracksBlock.java
鈹?  鈹?  鈹溾攢鈹€ Tier1SquadBarracksBlock.java
鈹?  鈹?  鈹溾攢鈹€ Tier2SquadBarracksBlock.java
鈹?  鈹?  鈹溾攢鈹€ Tier3SquadBarracksBlock.java
鈹?  鈹?  鈹斺攢鈹€ entity/
鈹?  鈹?      鈹溾攢鈹€ FleshFurnaceBlockEntity.java
鈹?  鈹?      鈹斺攢鈹€ SquadBarracksBlockEntity.java
鈹?  鈹溾攢鈹€ entity/
鈹?  鈹?  鈹斺攢鈹€ SculkSpikeEntity.java
鈹?  鈹溾攢鈹€ magic/spell/
鈹?  鈹?  鈹溾攢鈹€ SculkBindSpell.java
鈹?  鈹?  鈹斺攢鈹€ SquadSpell.java
鈹?  鈹溾攢鈹€ events/
鈹?  鈹?  鈹溾攢鈹€ SculkBindEvents.java
鈹?  鈹?  鈹斺攢鈹€ SquadFocusEvents.java
鈹?  鈹溾攢鈹€ menu/                       # 瀹瑰櫒 GUI 閫昏緫
鈹?  鈹?  鈹溾攢鈹€ ModMenuTypes.java
鈹?  鈹?  鈹斺攢鈹€ SquadBarracksMenu.java
鈹?  鈹溾攢鈹€ net/                        # 缃戠粶鍖?鈹?  鈹?  鈹溾攢鈹€ ModNetwork.java
鈹?  鈹?  鈹斺攢鈹€ SquadConfigC2SPacket.java
鈹?  鈹斺攢鈹€ utils/
鈹?      鈹溾攢鈹€ MinionSpawnHelper.java
鈹?      鈹斺攢鈹€ SquadChunkTickets.java
鈹斺攢鈹€ client/                        # 瀹㈡埛绔笓鐢?    鈹溾攢鈹€ ModClientEvents.java
    鈹溾攢鈹€ FleshFurnaceHUD.java
    鈹溾攢鈹€ gui/
    鈹?  鈹斺攢鈹€ SquadBarracksScreen.java
    鈹斺攢鈹€ render/
        鈹溾攢鈹€ SculkBidingModel.java
        鈹斺攢鈹€ SculkBidingRenderer.java
```

---

## 涓夈€佸垎灞傜害瀹?
| 鍖?| 鑱岃矗 | 绂佹 |
|----|------|------|
| `init/` | DeferredRegister + RegistryObject | 涓氬姟閫昏緫銆佸尶鍚嶅唴閮ㄧ被 |
| `common/item/` | 鐗╁搧浜や簰閫昏緫锛坲se/useOn锛?| 娉ㄥ唽閫昏緫 |
| `common/block/` | 鏂瑰潡琛屼负 + entity/ 鏂瑰潡瀹炰綋 tick | 瀹㈡埛绔唬鐮?|
| `common/entity/` | 瀹炰綋 AI銆佸姩鐢荤姸鎬佹満 | 娓叉煋浠ｇ爜 |
| `common/magic/` | Spell 鈫?spell/ 瀛愬寘 | 鐗╁搧娉ㄥ唽 |
| `common/events/` | `@Mod.EventBusSubscriber` | 娉ㄥ唽閫昏緫 |
| `common/menu/` | AbstractContainerMenu 瀹炵幇 | 娉ㄥ唽閫昏緫銆佹覆鏌?|
| `common/net/` | 缃戠粶鍖?encode/decode/handle | 涓氬姟閫昏緫 |
| `common/utils/` | 绾潤鎬佸伐鍏锋柟娉?| 瀹炰緥鐘舵€?|
| `client/` | 娓叉煋/HUD/GUI | 鏈嶅姟绔€昏緫 |
| `client/gui/` | Screen 瀹炵幇 | 鏈嶅姟绔€昏緫 |
| `client/render/` | 瀹炰綋妯″瀷 + 娓叉煋鍣?| 鏈嶅姟绔€昏緫 |

**鏂板姛鑳芥坊鍔犳祦绋嬶細**
1. 闇€瑕佹柊娉ㄥ唽椤?鈫?`init/` 涓嬫柊寤烘敞鍐岀被鎴栨墿灞曠幇鏈夌被
2. 鑷畾涔夎涓?鈫?`common/` 瀵瑰簲瀛愬寘涓嬪垱寤虹被
3. 鍏叡闈欐€侀€昏緫锛?琛?锛夆啋 `common/utils/` 鎻愬彇
4. 瀹㈡埛绔覆鏌?GUI 鈫?`client/` 涓嬪垱寤?5. 鍙厤缃暟鍊?鈫?`config/ModConfig.java` 鏂板閰嶇疆椤?
---

## 鍥涖€侀」鐩壒鏈夎璁″喅绛?
### 4.1 琛€鑲夌啍鐐夛紙Flesh Furnace锛夎緭鍑虹瓥鐣?- 鎵弿 8 鏍煎崐寰勫唴鐨勪笉绁ョ伀鐐紙Ominous Pyre锛夋潵纭畾鍏电
- 浼樺厛杈撳嚭鍒颁笅鏂瑰鍣ㄧ殑閫昏緫锛堟紡鏂?绠￠亾锛?- 閫氳繃 `ModTags.REQUIRES_TRAINING_STRUCTURE` 鏍囩杩囨护楂樼骇鍏电
- 瀹㈡埛绔?HUD 杩涘害鏉″悓姝?
### 4.2 鎵洸鑳氳儙 鈫?鎴愮啛鑳氳儙 杞寲閾捐矾
```
TwistedEmbryoItem (鍙抽敭鐢熸垚闅忔満 Neollager)
         鈫?   FleshFurnace (鐑瑰埗)
         鈫?MatureEmbryoItem (NBT 瀛樺疄浣撶被鍨? 鍙抽敭鐢熸垚鎸囧畾鍏电)
```

### 4.3 骞藉尶鏉熺細娉曟湳锛圫culkBindSpell锛夋祦绋?
```
Phase 1: 閿佸畾鐩爣 鈫?寮哄埗鍘熷湴锛圥ersistentData 瀛樺潗鏍囷級
Phase 2: 鍙敜 SculkSpike锛堣瑙夊疄浣擄級+ 澹版尝绮掑瓙 + 灏栧暩闊虫晥
Phase 3: 鍙敜鍘熺増 Warden锛岄€氳繃 Brain 璁板繂鎿嶄綔鎺у埗鍑哄湡鍔ㄧ敾銆佺閬佸湴銆佹敞鍏ユ劋鎬掑€?```

**鏋舵瀯鍐崇瓥**锛氫娇鐢ㄥ師鐗?Warden + Brain 鎿嶄綔锛岃€岄潪鑷畾涔?Warden 瀛愮被銆傝鎯呰 `goety-addon-dev` 鎶€鑳界涓冮儴鍒嗐€?
### 4.4 SculkBind 浜嬩欢澶勭悊

- **瀹氳韩**锛歚SculkBindEvents.handleSnare()` 鈥?姣?tick 浠?PersistentData 璇诲彇閿佸畾鍧愭爣锛屽己鍒?`setPos` 褰掍綅
- **Warden 鍙鎬?*锛歚SculkBindEvents.handleWardenVisibility()` 鈥?寤惰繜 15 tick 鎭㈠鍙锛岄槻姝㈠嚭鍦熷姩鐢婚甯ч棯鐜?
---

### 4.5 鐐瑰皢鍙帮紙Squad Barracks锛夌郴缁?
#### 4.5.1 鏍稿績鏋舵瀯

```
SquadBarracksBlock (abstract, BaseEntityBlock)
鈹溾攢鈹€ Tier1SquadBarracksBlock (6 config slots)
鈹溾攢鈹€ Tier2SquadBarracksBlock (12 config slots, 50% soul discount)
鈹斺攢鈹€ Tier3SquadBarracksBlock (18 config slots, 90% soul discount)
         鈫?owns
SquadBarracksBlockEntity 鈥?閰嶇疆妲?virtual) + 鍌ㄥ妲?ItemStack)
         鈫?opens
SquadBarracksMenu (AbstractContainerMenu, virtual config slots)
         鈫?renders
SquadBarracksScreen (AbstractContainerScreen, tiers use different textures)
```

#### 4.5.2 鍏靛洟鑱氭櫠缂栭槦鍙敜娴佺▼

```
SquadFocus (鍙抽敭缁戝畾鐐瑰皢鍙?鈫?NBT 瀛?bound pos/dim/tier/count)
         鈫?SquadSpell.SpellResult() 鈫?楠岃瘉缁戝畾 + 缁村害 + 鎵€鏈夋潈 + 鐏甸瓊
         鈫?barracks.consumeSquad() 鈫?娑堣垂鍌ㄥ妲芥垚鐔熻儦鑳?         鈫?MinionSpawnHelper.spawnTimedMinion() 鈫?鏈夊鍛戒粏浠?         鈫?SquadFocus.TAG_SUMMONER 鈫?鐢ㄤ簬 dismiss-all 鍙洖
```

**鏋舵瀯鍐崇瓥**锛?- 閰嶇疆妲芥槸铏氭嫙妲斤紙浠呭瓨 `ResourceLocation`锛夛紝涓嶅彲浜や簰锛涘偍澶囨Ы鏄湡瀹?`ItemStack` 妲戒綅
- C2S 缃戠粶鍖?(`SquadConfigC2SPacket`) 瀹炴椂鍚屾 GUI 鎿嶄綔鍒版湇鍔＄
- `SquadChunkTickets` 淇濊瘉璺ㄧ淮搴︾偣灏嗗彴鍖哄潡淇濇寔鍔犺浇锛堢敤浜庤繙绋嬪彫鍞わ級
- 涓夐樁鎶樻墸閫氳繃 `ModConfig.SQUAD_TIER2_DISCOUNT` 鍜?`SQUAD_TIER3_DISCOUNT` 閰嶇疆
- 鎸?Shift + 鍙抽敭鏂芥硶瑙﹀彂 dismiss-all锛坄SquadSpell.isShifting` 鍒嗘敮锛?
---

### 4.6 GUI 娓叉煋鍏抽敭缁忛獙 (SquadBarracks)

閫傜敤浜庣偣灏嗗彴 GUI 鎴栧叾浠?`AbstractContainerScreen` 瀛愮被銆?
#### `blit()` 蹇呴』浼犵汗鐞嗗疄闄呭昂瀵?
鍒嗘鎴彇鏃朵娇鐢ㄥ畬鏁撮噸杞斤細
```java
gui.blit(TEXTURE, x, y, destWidth, destHeight, uOffset, vOffset, uvWidth, uvHeight, texWidth, texHeight);
```

#### `imageHeight` 鍙樻洿鍚庢洿鏂?`inventoryLabelY`

```java
this.imageWidth = 194;
this.imageHeight = 168;
this.inventoryLabelY = this.imageHeight - 94;  // 鈫?瀹规槗閬楁紡
```

#### 鐢?`leftPos`/`topPos` 鑰岄潪閲嶅璁＄畻

`AbstractContainerScreen.init()` 宸插湪 `super.init()` 绠楀ソ锛岀洿鎺ュ紩鐢ㄣ€?
#### `render()` 蹇呴』璋冪敤 `renderBackground()`

```java
@Override
public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
    this.renderBackground(gui);
    super.render(gui, mouseX, mouseY, partialTick);
    this.renderTooltip(gui, mouseX, mouseY);
}
```

SquadBarracks 鏇惧洜鏈紶绾圭悊灏哄 + inventoryLabelY 鏈悓姝?鈫?UV 鏄犲皠閿欒銆佽儗鍖呮爣绛惧亸绉?2px銆?
---

### 4.7 Prototype 姝ラ珮绯荤粺 鈥?Mixin 瑕嗗啓 `maxUpStep()`

#### 闂

鍘熸柟妗堝湪 `PrototypeEvents.java` 涓瘡 tick 璋冪敤 `player.setMaxUpStep(1.5f)`锛堝啿鍒烘椂锛夊拰 `player.setMaxUpStep(0.6f)`锛堥潪鍐插埡鏃讹級銆傝繖瀵艰嚧姝ラ珮鍦?1.5 鍜?0.6 涔嬮棿棰戠箒缈昏浆锛屽紩璧烽€熷害寮傚父銆?
#### 鏍瑰洜

`Entity.maxUpStep()` 鏈変袱涓皟鐢ㄩ摼锛屼笖 `Player` 鏈夌壒娈婄粫杩囷細

| 浠ｇ爜璺緞 | 璋冪敤鏂瑰紡 | 璇存槑 |
|---------|---------|------|
| `Entity.collide()` (Forge) | 鈫?`getStepHeight()` 鈫?`maxUpStep()` + `STEP_HEIGHT_ADDITION` | Forge 鎺ㄨ崘璺緞 |
| `Player.maybeBackOffFromEdge()` | **鐩存帴璋冪敤 `this.maxUpStep()`** | 鏄庣‘娉ㄩ噴涓嶄娇鐢?`getStepHeight()` (forge#8922) |
| `Player.isAboveGround()` | **鐩存帴璋冪敤 `this.maxUpStep()`** | 鍚屼笂 (forge#9376) |

浠呯敤 `setMaxUpStep()` 鎴栨搷浣?`STEP_HEIGHT_ADDITION` 灞炴€ф棤娉曡鐩?`maybeBackOffFromEdge` 鍜?`isAboveGround`銆?
#### 瑙ｅ喅鏂规锛歁ixin 娉ㄥ叆 `Entity.maxUpStep()`

**鏂板缓 Mixin** `PrototypeStepHeightMixin.java`锛宍@Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)`锛屽綋 `PrototypeItem.hasAnyPrototype(player)` 鏃惰繑鍥?`1.5f`銆?
```java
@Mixin(Entity.class)
public abstract class PrototypeStepHeightMixin {
    @Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
    private void gp$overrideMaxUpStep(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Player player && PrototypeItem.hasAnyPrototype(player)) {
            cir.setReturnValue(1.5f);
        }
    }
}
```

#### 鏁堟灉

- **涓や釜璋冪敤閾鹃兘瑕嗙洊**锛歅layer 鐨勭洿鎺ヨ皟鐢ㄥ拰 Forge 鐨?`getStepHeight()` 鍧囩粡杩?`maxUpStep()`
- **鏃犻渶 per-tick 鍐欏叆**锛氱Щ闄?`PrototypeEvents.java` 涓袱涓?`setMaxUpStep()` 璋冪敤
- **姝ラ珮鎭掑畾 1.5**锛氫笉闅?sprint 鐘舵€佺炕杞紝娑堥櫎閫熷害寮傚父
- **鍗虫椂鐢熸晥**锛氱┛涓?鑴变笅 prototype 鏃?Mixin 瀹炴椂鍒ゆ柇 `hasAnyPrototype`锛屾棤寤惰繜

#### 娉ㄦ剰浜嬮」

- Mixin 娉ㄥ唽鍦?`mixins` 鏁扮粍锛堟湇鍔＄+瀹㈡埛绔級锛屽洜涓?`maxUpStep()` 鍦ㄤ袱渚ч兘浣跨敤
- `PrototypeItem.hasAnyPrototype()` 妫€鏌ヨ兏鐢叉牸 + Curios 妲斤紝绾冲叆鐑矾寰勬€ц兘鍙帴鍙?- 鑻ュ叾浠栨ā缁勫悓鏃舵敞鍏?`maxUpStep()`锛屾寜 Mixin 浼樺厛绾ф墽琛岋紱`cancellable=true` 淇濊瘉涓嶈瑕嗙洊

---

## 浜斻€佺姝簨椤?
| 绂佹 | 姝ｇ‘鍋氭硶 |
|------|----------|
| 涓荤被涓啓涓氬姟閫昏緫 | 鏀惧叆 `common/` 瀵瑰簲鍖?|
| 娉ㄥ唽绫讳腑鍐欏尶鍚嶅唴閮ㄧ被涓氬姟 | 鍒涘缓鐙珛涓氬姟绫伙紝娉ㄥ唽绫讳粎寮曠敤 |
| 宸ュ叿鏂规硶鏀惧湪涓氬姟鍖呮牴鐩綍 | 鏀惧叆 `common/utils/` |
| 鎵嬪姩缁戜粏浠庝富浜?| 蹇呴』鐢?`MobUtil.summonTame()` |
| Goety 宸叉湁鎺ュ彛/宸ュ叿鏃惰嚜琛岄噸鍐?| 浼樺厛澶嶇敤 Goety |
| 淇敼 build.gradle 渚濊禆閰嶇疆 | 淇濇寔鐜版湁閰嶇疆 |
| 鍒涘缓绌洪鐣欎簨浠剁被 | 闇€瑕佹椂鍐嶅垱寤?|