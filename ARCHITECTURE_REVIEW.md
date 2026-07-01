# 鏋舵瀯瀹℃煡鎶ュ憡锛堜慨姝ｇ増锛?

> 瀹℃煡鏃ユ湡: 2026-06-27 | 淇鐗?
> 瀹℃煡鑼冨洿: GoetyMech 妯″潡鍏ㄩ儴 161 涓?Java 婧愭枃浠?
> 瀹℃煡鏂规硶: 鏋舵瀯鍒嗗眰璇勪及 + 鏂囨。浜ゅ弶楠岃瘉
> 鏂囨。鍙傝€? `docs/items/` (51+ 鏂囦欢), `docs/incidents/` (13 鏂囦欢), `docs/agents/`, `CONTEXT.md`, `PROTOTYPE_ARCHITECTURE.md`, `time-focus-pouch-architecture.md`, `openspec/` (35+ 鏂囦欢)

---

## 鍙樻洿璁板綍

| 鍘熸姤鍛婇棶棰?| 淇缁撴灉 | 渚濇嵁 |
|-----------|---------|------|
| F1. DEBUG 鏃ュ織 (馃敶) | 馃煛 **闄嶇骇** | PROTOTYPE_ARCHITECTURE.md:525 澹版槑涓?DEBUG 绾у埆锛堝紑鍙戜腑锛夛紝浠ｇ爜璇敤 `LOGGER.info` |
| F2. Mixin 缁ф壙鍏崇郴 (馃敶) | 鉂?**璇姤锛屾挙閿€** | `PrototypePlayerMixin extends LivingEntity` 鏄湁鏁?Mixin 妯″紡锛屼负缂栬瘧鎻愪緵 `zza`/`jumping` 瀛楁 |
| F3. 椤圭洰鍚?(馃敶) | 馃數 **闄嶇骇** | 鐩綍鍚嶆潵鑷?Architectury 妯℃澘锛屼笉褰卞搷鍔熻兘 |
| F9. CrazedIllager 鎴樻枟 (馃數) | 鉂?**璇姤锛屾挙閿€** | 鑷激 1 HP 鍦?`crazed-illager.md:7` 涓褰曚负璁捐鍐崇瓥锛沗invulnerableTime` 鎿嶆帶鏄父瑙佹ā缁勬垬鏂楁ā寮?|
| F11. Mixin 鎬绘暟 (馃數) | 馃攧 **淇** | 瀹為檯 25 涓紙13 client + 12 mixins锛夛紝闈?24 |

**鍑€鍙樺寲锛氬師 11 涓彂鐜?鈫?淇鍚?8 涓紙3 鎾ら攢锛? 闄嶇骇锛? 淇鏁板瓧锛? 鏂板锛?*

---

## 涓€銆佹€讳綋璇勫垎

| 缁村害 | 璇勫垎 | 璇存槑 |
|------|------|------|
| 鍖呯粨鏋?| 猸愨瓙猸愨瓙猸?| 娓呮櫚鍒嗗眰锛宍init/common/client/config/compat` 鑱岃矗鏄庣‘ |
| 娉ㄥ唽妯″紡 | 猸愨瓙猸愨瓙猸?| 涓€鑷翠娇鐢?DeferredRegister锛屾棤鍖垮悕鍐呴儴绫讳笟鍔￠€昏緫 |
| 浠ｇ爜璐ㄩ噺 | 猸愨瓙猸愨瓙 | 澶ч儴鍒嗕唬鐮佽鑼冿紝`LOGGER.info` 鐢ㄤ簬璋冨紡杈撳嚭鍙敼杩?|
| 鍙淮鎶ゆ€?| 猸愨瓙猸愨瓙猸?| 60+ 鐗╁搧鏂囨。銆?3 鏉′簨鏁呰褰曘€佸畬鏁?openspec 娴佺▼ |
| 鎬ц兘 | 猸愨瓙猸?| 瀛樺湪鏄庢樉鐨勭儹璺緞鎬ц兘闂锛堝叏閲忛亶鍘嗭級 |
| 鏋勫缓绯荤粺 | 猸愨瓙猸愨瓙 | 鏍囧噯 Forge 47.x 鏋勫缓锛屽墠缃０鏄庢竻鏅?|
| Mixin 娌荤悊 | 猸愨瓙猸?| 25 涓?mixin锛岄儴鍒嗗瓨鍦ㄧ敓浜х幆澧冨奖鍝?|

**缁煎悎璇勭骇: A-锛堣壇濂斤紝鏂囨。浣撶郴浼樼锛屽叧閿穿婧冮棶棰樺凡璇嗗埆锛?*

---

## 浜屻€佹灦鏋勪寒鐐?

### 鉁?鏂囨。浣撶郴浼樼

椤圭洰鍖呭惈瀹屾暣鐨勬枃妗ｅ眰娆★細
- `docs/items/` 涓?51 涓墿鍝?鏂瑰潡/瀹炰綋/缃戠粶/浜嬩欢鏂囨。锛屾瘡涓枃浠跺惈韬唤鎻忚堪 + timeline
- `docs/incidents/` 璁板綍 13 娆¤皟璇曠粡楠岋紝鍚?`status: verified/disputed/superseded` 鐘舵€佹爣璁?
- `docs/agents/` 涓?AI 宸ュ叿鎻愪緵鎿嶄綔涓婁笅鏂?
- `PROTOTYPE_ARCHITECTURE.md` (525 琛? 璇︾粏鎻忚堪鍘熷瀷绯荤粺璁捐锛屽寘鍚厤缃弬鏁般€佺姸鎬?tag 鎬昏銆侀琛屽姏瀛﹀叕寮忋€佹椂搴忓浘
- `time-focus-pouch-architecture.md` (307 琛? 鎻忚堪鏃跺仠绯荤粺鏋舵瀯锛屽惈瀹屾暣鏃跺簭鍥惧拰璁捐鍐崇瓥

### 鉁?娓呮櫚鐨勫寘鍒嗗眰

```
init/         鈫?绾敞鍐岋紝鏃犱笟鍔￠€昏緫
common/       鈫?绾笟鍔￠€昏緫锛坕tem/block/entity/magic/events锛?
client/       鈫?绾鎴风浠ｇ爜锛坮ender/gui/HUD锛?
config/       鈫?闆嗕腑閰嶇疆锛岀儹璺緞缂撳瓨
compat/       鈫?妯＄粍鍏煎锛圝EI/Curios/Caelus锛?
```

娉ㄥ唽绫讳笌涓氬姟绫讳弗鏍煎垎绂伙紝绗﹀悎 "Registration 鈫?Behavior" 鑱岃矗杈圭晫銆?

### 鉁?缃戠粶灞傝璁¤壇濂?

C2S/S2C 瀛愬寘鍒嗙锛? C2S + 8 S2C 鍖咃紝姣忎釜鍖呯嫭绔?encode/decode/handle锛屾棤涓氬姟閫昏緫娉勬紡鍒扮綉缁滃眰銆?

### 鉁?妯＄粍鍏煎灞傞殧绂?

- Caelus 閫氳繃绫诲弽灏勫姞杞藉疄鐜版潯浠舵敞鍐?
- JEI 鎻掍欢鐙珛鍖?`compat/jei/`
- Curios 妲戒綅閫氳繃 data pack 鏍囩閰嶇疆
- Goety 浠呴€氳繃 API 浜や簰锛屾棤鍐呴儴绫?hack

### 鉁?閰嶇疆鐑矾寰勭紦瀛?

- `ModConfig.refreshCache()` 鍦?`ModConfigEvent.Reloading` 鏃跺埛鏂?
- `cachedLinkRange` 鍜?`cachedFurnaceCookingTime` 鍑忓皯姣?tick 閰嶇疆璁块棶寮€閿€

### 鉁?Openspec 宸ヤ綔娴?

35+ openspec 鏂囦欢瑕嗙洊浜?proposal 鈫?design 鈫?tasks 鈫?spec 鐨勫畬鏁存祦绋嬶紝宸插綊妗?6 涓畬鏁?change锛屾椿璺?4 涓?change銆傛瘡涓?change 鏈夌嫭绔嬬殑 `.openspec.yaml` 鍏冩暟鎹€?

---

## 涓夈€佺粺璁′慨姝?

| 鎸囨爣 | 鍘熷€?| 淇鍊?|
|------|------|--------|
| Mixin 鎬绘暟 | 24 (12 client + 12 server) | 25 (13 client + 12 mixins) |
| 娉ㄥ唽鐗╁搧鏁?| 26+ | 26+ |
| 娉ㄥ唽鏂瑰潡鏁?| 6 | 6 |
| 娉ㄥ唽瀹炰綋鏁?| 4 | 4 |
| 娉ㄥ唽鏂瑰潡瀹炰綋 | 5 | 5 |
| 缃戠粶鍖?| 16 (8 C2S + 8 S2C) | 16 (8 C2S + 8 S2C) |
| 浜嬩欢澶勭悊鍣?| 8 | 8 |
| 宸ュ叿绫?| 6 | 6 |
| 鍏煎灞傛枃浠?| 9 | 9 |
| 娓告垙娴嬭瘯 | 2 | 2 |
| 鏂囨。鏂囦欢 | 60+ | ~80 (items 51 + incidents 13 + agents 3 + arch docs 2 + openspec 35) |

**Mixin 鍒嗙被鏄庣粏**锛?

| 鍒嗙被 | 鏂囦欢 | 鏁伴噺 |
|------|------|------|
| 瀹㈡埛绔紙`"client"` 鏁扮粍锛?| Blaze3DMixin, CameraMixin, ClientLevelMixin, GameRendererMixin, GuiMixin, HumanoidArmorLayerMixin, ItemInHandRendererMixin, LevelRendererMixin, MinecraftMixin, ParticleEngineMixin, TimerAccessor, TutorialMixin, EntityMixin | **13** |
| 閫氱敤/鏈嶅姟绔紙`"mixins"` 鏁扮粍锛?| CollectingNeighborUpdaterMixin, InstantNeighborUpdaterMixin, LevelMixin, LivingEntityHurtMixin, LivingEntityTickMixin, ItemEntityPrototypeMixin, ItemEntityVoidMixin, PrototypePlayerMixin, PrototypeStepHeightMixin, PrototypeTravelMixin, MixinWallRunTravel, EntityOnGroundAccessor | **12** |

---

## 鍥涖€佸叧閿彂鐜?

### 馃敶 涓ラ噸 (Must Fix)

#### F1. `SquadConfigC2SPacket` 鍐呭瓨娉勬紡

- **浣嶇疆**: `common/net/SquadConfigC2SPacket.java:19`
- **闂**: `static final Map<UUID, Long> lastPacketTick` 浠呭湪閫熺巼闄愬埗鏃?`put`锛屾案涓?`remove`銆傜帺瀹堕€€鍑哄悗 UUID 鏉＄洰娈嬬暀鍦?ConcurrentHashMap 涓紝闅忚繍琛屾椂闂存寔缁闀裤€?
- **褰卞搷**: 鑱旀満鏈嶅姟鍣ㄥ唴瀛樻硠婕忥紝48h+ 鍚庡彲杈炬暟鍗冧釜宸茬绾跨帺瀹舵潯鐩€?
- **淇**: `@SubscribeEvent PlayerEvent.PlayerLoggedOutEvent` 涓墽琛?`lastPacketTick.remove(event.getEntity().getUUID())`銆?

#### F2. `TEST` Logger 鍚嶆薄鏌?

- **浣嶇疆**: `block/entity/RemoteEngineBlockEntity.java:56`, `StressReaderBlockEntity.java:75`
- **闂**: 浣跨敤 `LoggerFactory.getLogger("TEST")` 鑰岄潪 `GoetyMechMod.LOGGER`銆傚鏋?logback.xml 鎸?Logger 鍚嶉厤缃繃婊わ紝`"TEST"` 鍚嶇О鍙兘缁曡繃婊ょ瓥鐣ワ紙濡傜敓浜х幆澧冮厤缃负 `com.GoetyMech.addon` 绾у埆=WARN锛屽垯 `TEST` 涓嶅彈褰卞搷缁х画杈撳嚭 INFO锛夈€?
- **褰卞搷**: 鏃ュ織绠＄悊澶辨帶椋庨櫓銆?
- **淇**: 鏀逛负 `GoetyMechMod.LOGGER`銆?

---

### 馃煛 涓瓑闂 (Should Fix)

#### F3. 娈嬬暀璋冭瘯鏃ュ織锛垀40 澶?INFO 绾у埆锛?

- **鏂囦欢鍒嗗竷**:
  - `PrototypeEvents.java`锛垀30 澶勶細`D START`, `PROTO_DIAG`, `SprintTick`, `SlamImpact` 绛夛級
  - `PrototypePlayerMixin.java`锛? 澶勶細`MIXIN_AISTEP_HEAD/RETURN`锛屾瘡 tick/鐜╁锛?
  - `PrototypeRenderEvents.java`锛垀5 澶勶細`CLIENT_CAMERA`, `CLIENT_WALL`锛?
  - `CrushingWheelEntity.java`锛垀10 澶勶細`[WheelTest]`锛?
  - `ModClientEvents.java`锛? 澶勶細`MOD_CLIENT` 璇敤 `LOGGER.error`锛?
- **鏂囨。瀵圭収**: PROTOTYPE_ARCHITECTURE.md:525 澹版槑"PlayerTickEvent.START 杩樿緭鍑?zza/onGround/isFallFlying锛圖EBUG 绾у埆锛?銆備絾浠ｇ爜浣跨敤 `LOGGER.info()` 鑰岄潪 `LOGGER.debug()`銆?
- **寤鸿**: 缁熶竴闄嶇骇涓?`LOGGER.debug()` 鎴栫Щ闄ゃ€傚紑鍙戜腑鐨勮皟璇曟棩蹇楀簲浣跨敤 DEBUG 绾у埆锛岀敓浜х幆澧冮粯璁や笉杩囨护 INFO銆?

#### F4. 閰嶇疆鍔犺浇渚濊禆娆″簭鑴嗗急

- **浣嶇疆**: `config/ModConfig.java:613-621`
- **闂**: `loadConfig()` 鍦?`ModLoadingContext.registerConfig()` 娉ㄥ唽鍓嶄娇鐢?`CommentedFileConfig` 鎵嬪姩鍔犺浇锛岀粫杩?Forge 閰嶇疆鐢熷懡鍛ㄦ湡浜嬩欢銆?
- **鏂囨。瀵圭収**: 婧愮爜娉ㄩ噴璇存槑"浠跨収 Goety SpellConfig 鐨勫仛娉?銆傛妯″紡鍦?Goety 涓凡楠岃瘉锛屼絾渚濊禆浜?Forge 鍐呴儴瀹炵幇缁嗚妭锛岃法鐗堟湰鍙兘澶辨晥銆?
- **寤鸿**: 浣跨敤 `ModConfigEvent.Loading` 浜嬩欢椹卞姩鍔犺浇锛屾垨淇濈暀浣嗘坊鍔犲崟鍏冩祴璇曡鐩栧吋瀹规€с€?

#### F5. `findAllBoundFoci` 鏃犵紦瀛?

- **浣嶇疆**: `SquadFocus.java:120-130`
- **闂**: 姣忔鐜╁ GUI 鎿嶄綔 `refreshBoundFoci()` 鈫?`findAllBoundFoci()` 閬嶅巻鎵€鏈夊湪绾跨帺瀹剁殑 inventory + armor + offhand銆傞珮鐜╁鏁版湇鍔″櫒涓嬶紝姣忔 GUI 浜や簰瑙﹀彂 O(n脳m) 鎵弿銆?
- **寤鸿**: 寮曞叆 `InventoryChangeEvent` 鐩戝惉鍣ㄥ閲忔洿鏂般€?

#### F6. 缃戠粶鍗忚鐗堟湰纭紪鐮?

- **浣嶇疆**: `ModNetwork.java:27`
- **闂**: `PROTOCOL_VERSION = "1"` 纭紪鐮併€傜綉缁滃寘缁撴瀯鍙樻洿鍚庡繕璁版洿鏂板彲鑳藉鑷?C2S/S2C 瑙ｇ爜閿欎綅銆?
- **寤鸿**: 浣跨敤鏋勫缓鐗堟湰娉ㄥ叆鎴?`@Config` 瀹氫箟銆?

---

### 馃數 鏀硅繘寤鸿 (Nice to Have)

#### F7. `GoetyMech.mixins.json` 涓?accessor 涓?injector 娣峰悎

- **浣嶇疆**: `GoetyMech.mixins.json:22-35`
- **闂**: `TimerAccessor`锛坅ccessor 鎺ュ彛锛夊拰 `EntityOnGroundAccessor`锛坅ccessor 鎺ュ彛锛夋斁鍦?`"client"` 鍜?`"mixins"` 鏁扮粍涓紝涓庢敞鍏ョ偣娣峰悓銆侻ixin 瑙勮寖寤鸿鍒嗙鍒?`"injectors"` 鍒嗗尯銆?
- **褰卞搷**: 鍙鎬ч檷浣庯紝鏃犳硶涓€鐩簡鐒跺尯鍒嗚闂櫒鍜屾敞鍏ョ偣銆?

#### F8. 鏂囨。涓庝唬鐮佽交寰劚鑺?

- **闂**: `time-focus-pouch-architecture.md:307` 鍒楀嚭浜?`mixin/ServerLevelMixin.java`锛屼絾璇ユ枃浠朵笉鍦?`src/` 涓紝涔熸湭鍦?`mixins.json` 涓敞鍐屻€傛枃妗ｈ褰曚簡瑙勫垝鎴栧凡鍒犻櫎鐨勬枃浠躲€?
- **褰卞搷**: 鏂拌础鐚€呴槄璇绘枃妗ｅ悗鍙兘璇俊璇ユ枃浠跺瓨鍦ㄣ€?
- **寤鸿**: 瀹℃煡骞舵洿鏂?`time-focus-pouch-architecture.md` 鐨勬枃浠舵竻鍗曘€?

#### F9. 娓告垙娴嬭瘯瑕嗙洊涓嶈冻

- **浣嶇疆**: `gametest/`
- **闂**: 161 涓?Java 婧愮爜鏂囦欢浠?2 涓祴璇曠被锛堥琛屾祴璇?+ 璺戝娴嬭瘯锛夛紝浠呰鐩?Prototype 瀛愮郴缁熴€傝鑲夌啍鐐夈€佺偣灏嗗彴銆佸菇鍖挎潫缂氭硶鏈瓑鏃犳祴璇曚繚鎶ゃ€?
- **寤鸿**: 瀵瑰叧閿郴缁熼€愭娣诲姞 GameTest銆?

#### F10. Mixin 璋冪敤鑰呮ā寮忎笉缁熶竴

- **浣嶇疆**: `PrototypeTravelMixin.java:22, 70, 131`
- **闂**: 涓変釜 `@Inject` 鍚勮嚜鐙珛璋?`PrototypeItem.hasAnyPrototype(player)`锛屾棤缂撳瓨銆?
- **寤鸿**: 鍚堝苟涓轰竴涓墠缃?injector锛岀紦瀛樼粨鏋滀緵鍚庝袱涓娇鐢ㄣ€?

---

## 浜斻€佽鍔ㄥ缓璁紭鍏堢骇

| 浼樺厛绾?| 闂 | 宸ヤ綔閲?| 绫诲瀷 |
|--------|------|--------|------|
| P0 | F1: `SquadConfigC2SPacket` 鍐呭瓨娉勬紡 | 5 鍒嗛挓 | 馃悰 宕╂簝绾?|
| P1 | F3: ~40 澶勬棩蹇楅檷绾?DEBUG | 30 鍒嗛挓 | 馃棏 娓呯悊 |
| P1 | F2: TEST Logger 鍚嶁啋鏀圭敤 MOD LOGGER | 5 鍒嗛挓 | 馃棏 娓呯悊 |
| P2 | F5: `findAllBoundFoci` 鍔犵紦瀛?| 30 鍒嗛挓 | 鈿?鎬ц兘 |
| P2 | F4: 閰嶇疆鍔犺浇鏀逛簨浠堕┍鍔?| 20 鍒嗛挓 | 馃彈 鍋ュ．鎬?|
| P3 | F9: 娓告垙娴嬭瘯瑕嗙洊 | 娓愯繘 | 馃И 璐ㄩ噺 |
| P3 | F6+F7+F8+F10: 娓愯繘鏀硅繘 | 娓愯繘 | 馃帹 浠ｇ爜璐ㄩ噺 |
