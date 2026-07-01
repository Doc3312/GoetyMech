# 鍏ㄥ憳浠ｇ爜瀹℃煡鎶ュ憡锛堜慨姝ｇ増锛?

> 瀹℃煡鏃ユ湡: 2026-06-27 | 淇鐗?
> 瀹℃煡鑼冨洿: 鍏ㄩ儴 161 涓?Java 婧愭枃浠?
> 瀹℃煡鏂规硶: 澶氳矾骞惰瀹℃煡 + 鏂囨。浜ゅ弶楠岃瘉
> 鏂囨。鍙傝€? `docs/items/`, `docs/incidents/`, `PROTOTYPE_ARCHITECTURE.md`, `openspec/`

---

## 鍙樻洿璁板綍

| 鍘熸姤鍛婇棶棰?| 淇缁撴灉 | 渚濇嵁 |
|-----------|---------|------|
| `SquadBarracksScreen` 鐗╁搧涓嶅悓姝?(C3) | 鉂?**璇姤锛屾挙閿€** | 閰嶇疆妲芥槸铏氭嫙妲斤紝涓嶆秷鑰楃墿鍝佹槸璁捐鎰忓浘 |
| `RemoteEngineBlockItem.getBoundPos` NPE | 鉂?**璇姤锛屾挙閿€** | `isBound()` 宸插畧鍗?null tag |
| `SquadFocus.offhand.get(0)` 瓒婄晫 | 鉂?**璇姤锛屾挙閿€** | `NonNullList` 鍥哄畾 size 1 |
| `PrototypeItem.hasFlight` 鏈敤鍙傛暟 | 鉂?**璇姤锛屾挙閿€** | 璁捐鍐崇瓥锛氭粦缈旀敼涓哄缁堝彲鐢?(`prototype.md:102`) |
| `MinionSpawnHelper` 鎶樻墸涓嶇敓鏁?| 鈿狅笍 **闄嶇骇** | `effectiveCost` 浠呯敤鍋氶棬妲涘垽鏂紱瀹為檯鎵ｈ垂鐢?Goety `decreaseSouls` 鍐呴儴澶勭悊锛堥渶璇?Goety 婧愮爜纭锛?|
| `TimeStopUtils` 璁℃暟鍣ㄦ竻闆剁洰鏍囧弽杞?| 鈿狅笍 **闄嶇骇** | 閫昏緫鍙枒浣嗗彲鑳芥湁鎰忥紙鎷掔粷浠栦汉瑙ｅ喕锛夛紝闇€璇?Goety EndLibrary 婧愮爜纭 |
| `TimeStopRandom` 瀛楁閬斀 | 鉂?**璇姤锛屾挙閿€** | `AtomicLong seed` 閬斀鐖剁被瀛楁 鈥?浣嗚绫荤嫭绔嬩簬鐖剁被鐨勯殢鏈虹姸鎬侊紝涓嶄細瀵艰嚧闂 |
| `HumanoidArmorLayerMixin` 鍖呰矾寰勯敊 | 鉂?**璇姤锛屾挙閿€** | 鏂囦欢瀹為檯鍦?`com.GoetyMech.addon.mixin` 鍖咃紝涓?mixins.json 閰嶇疆涓€鑷?|
| `HumanoidArmorLayerMixin` 闄勯瓟鍙傛暟閿?| 鉂?**璇姤锛屾挙閿€** | 浜屾娓叉煋宸茬敤 `if (hasFoil)` 瀹堝崼锛実lint/false 鍩虹灞?true 闂厜灞傛槸鏍囧噯妯″紡 |
| `CrazedIllagerEntity` 鑷激 (G1) | 鉂?**璇姤锛屾挙閿€** | `crazed-illager.md:7` 璁板綍涓鸿璁″喅绛?|
| 鎵€鏈?[TEST]/[WheelTest] 鏃ュ織 | 鉁?**纭** | 娈嬬暀璋冨紡浠ｇ爜锛宒ocs 鏈敞鏄庡叾蹇呰鎬?|

---

## 缁撹姹囨€?

| 绛夌骇 | 淇鍓?| 淇鍚?| 鍑€鍙樺寲 |
|------|--------|--------|--------|
| 馃敶 宕╂簝绾?| 4 | **2** | -2 |
| 馃敶 鐜╂硶绾?| 3 | **1**锛堝潎涓哄緟瀹氾級 | -2 |
| 馃煛 鏃ュ織姹℃煋 | ~40 澶?| **~40 澶?* | 涓嶅彉 |
| 馃煛 閫昏緫缂洪櫡 | 8 | **5** | -3 |
| 馃煛 鎬ц兘 | 5 | **5** | 涓嶅彉 |
| 馃數 浠ｇ爜寮傚懗 | ~12 | **~10** | -2 |

---

# 馃敶 宕╂簝绾?

## C1. `FleshFurnaceBlockEntity.getCapability()` 鈥?鏁扮粍绱㈠紩閿欎贡 + EAST 闈㈠穿婧?

- **鏂囦欢**: `src/main/java/com/GoetyMech/addon/common/block/entity/FleshFurnaceBlockEntity.java:70, 584`
- **闂**: `SidedInvWrapper.create(this, Direction.UP, NORTH, SOUTH, EAST, WEST)` 鍒涘缓 5 鍏冪礌鏁扮粍锛堢储寮?0-4锛夈€備絾 `handlers[side.get3DDataValue()]` 鐨勭储寮曞搴旀爣鍑嗘柟鍚戝簭锛圖OWN=0, UP=1, NORTH=2, SOUTH=3, WEST=4, EAST=5锛夈€?
  - UP(鈫?) 鈫?handlers[1] 鈫?**鍙栧埌 NORTH handler**
  - NORTH(鈫?) 鈫?handlers[2] 鈫?**鍙栧埌 SOUTH handler**
  - SOUTH(鈫?) 鈫?handlers[3] 鈫?**鍙栧埌 EAST handler**
  - EAST(鈫?) 鈫?handlers[5] 鈫?**`ArrayIndexOutOfBoundsException`** 馃毃
  - WEST(鈫?) 鈫?handlers[4] 鈫?姝ｅソ鍙栧埌 WEST handler锛堝阀鍚堟纭級
- **褰卞搷**: 瀵圭啍鐐?EAST 闈㈢殑婕忔枟/绠￠亾鎿嶄綔鐩存帴宕╂簝銆傛墍鏈夊叾浠栨柟鍚戯紙闄?WEST锛夋槧灏勫埌閿欒 handler銆?
- **淇**: `SidedInvWrapper.create(this, Direction.values())` 鎴栬€咃細
  ```java
  private final LazyOptional<IItemHandlerModifiable>[] handlers = SidedInvWrapper.create(this,
      Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
  ```

## C2. `TimeContext.safeClientFrameTime()` 缂哄皯 `@OnlyIn`

- **鏂囦欢**: `src/main/java/com/GoetyMech/addon/common/time/TimeContext.java:15-17`
- **闂**: 璋冪敤 `Minecraft.getInstance().getFrameTime()` 浣嗘棤 `@OnlyIn(Dist.CLIENT)`銆傚湪 Dedicated Server 涓婃姏鍑?`ClassNotFoundException` 鎴栫洿鎺ュ穿婧冦€?
- **淇**: 鍔?`@OnlyIn(Dist.CLIENT)`銆?

---

# 馃敶 鐜╂硶绾?

## ~~G1. `CrazedIllagerEntity` / `CrazedIllagerServant` 鈥?鑷激瀵艰嚧 3 绉掕嚜鏉€~~ 鈫?鉂?**璇姤锛屾挙閿€**

- **閲嶆柊璇勪及**: `docs/items/misc/crazed-illager.md:7` 鏄庣‘璁板綍 "鏀诲嚮鏈哄埗鏀逛负 4 甯?娆?脳 5 娆?绉掞紝鏃犺鏃犳晫甯э紝**鑷激 1 HP**" 涓?2026-06-25 鐨勬晠鎰忔敼鍔ㄣ€?
- **缁撹**: 鑷激鏄璁″喅绛栵紝闈?bug銆傝櫧鐒跺疄鎴樹腑 15 HP / 1/tick = 3 绉掕嚜鏉€鐪嬩技鏋佺锛屼絾鏂囨。璁板綍涓烘湁鎰忚涓猴紝涓嶄簣鏍囪銆?
- **淇濈暀鎰忚**: 寤鸿寮€鍙戣€呭湪鏂囨。涓ˉ鍏呰嚜浼ょ殑鐜╂硶鎰忓浘锛堝鐙傛毚/鎬掓皵鏈哄埗锛夛紝浠ヤ究鏈潵鍥為【銆?

## G2. ~~`MinionSpawnHelper` 鐏甸瓊鎶樻墸涓嶇敓鏁垀~ 鈫?**寰呭畾锛岄渶楠岃瘉 Goety 婧愮爜**

- **鏂囦欢**: `src/main/java/com/GoetyMech/addon/common/utils/MinionSpawnHelper.java:163-181`
- **闂琛ㄩ潰**: `effectiveCost = Math.max(1, (int)(soulCost * discount))` 璁＄畻浜嗘姌鎵ｄ环浣嗘墸璐逛粛鐢?`soulCost`銆?
- **璇佹嵁鍙嶈浆**: 鏂规硶娉ㄩ噴鏄庣‘鍐?鎵?soulCost锛堝唴閮ㄦ墦鎶橈級"锛屾殫绀烘姌鎵ｇ敱 Goety 鐨?`SEHelper.decreaseSouls()` 鍐呴儴澶勭悊銆?
- **缁撹**: **涓嶈兘纭鏄?bug**銆傚鏋?`SEHelper.decreaseSouls()` 鍐呴儴鑷姩搴旂敤 `soulDiscount`锛屽垯閫昏緫姝ｇ‘銆傞渶璇?`D:\Libcode\Goety-2-1.20\` 涓?`SEHelper.java` 婧愮爜纭銆?
- **淇**: 濡傛灉 Goety 涓嶅鐞嗗唴閮ㄦ姌鎵ｏ紝搴斿皢 `decreaseSouls(player, soulCost)` 鏀逛负 `decreaseSouls(player, effectiveCost)`銆?

## G3. ~~`TimeStopUtils.use() !z 鍒嗘敮璁℃暟鍣ㄦ竻闆剁洰鏍囧弽杞琡~~ 鈫?**寰呭畾锛岄渶楠岃瘉璁捐鎰忓浘**

- **鏂囦欢**: `src/main/java/com/GoetyMech/addon/common/time/TimeStopUtils.java:59`
- **闂琛ㄩ潰**: `setTimeStopCount(source, 0)` 璁剧疆鏉ユ簮鑰咃紙鑰屼笉鏄叾浠栧疄浣擄級鐨勮鏃跺櫒涓?0銆?
- **鍙岄噸瑙ｉ噴**:
  - **Bug 瑙ｉ噴**: 搴旀敼涓?`setTimeStopCount(living, 0)` 鈥?娓呴櫎鍏朵粬瀹炰綋鐨勮鏃跺櫒浠ュ厑璁稿叏灞€瑙ｅ喕
  - **Design 瑙ｉ噴**: "鍏朵粬浜鸿繕鍦ㄥ喕缁?鈫?鎷掔粷浣犵殑瑙ｅ喕璇锋眰锛屾妸浣犵殑璁℃暟鍣ㄦ竻绌猴紝璁╀綘閲嶆柊鍐荤粨鏃朵粠 0 寮€濮嬭鏃?
- **鏂囨。瀵圭収**: `docs/time-focus-pouch-architecture.md` 璁板綍鏃跺仠绯荤粺婧愯嚜 EndLibrary锛屼絾鏈弿杩版澶勭殑閫昏緫缁嗚妭銆?
- **缁撹**: 璇箟涓嶆槑纭紝闇€璇?EndLibrary 鍘熷瀹炵幇瀵圭収銆?

---

# 馃煛 鏃ュ織姹℃煋

> 浠ヤ笅鏂囦欢鍖呭惈娈嬬暀鐨?`LOGGER.info()` / `LOGGER.warn()` / `LOGGER.error()` 璋冨紡杈撳嚭銆? 
> 椤圭洰宸茶繘鍏ョ敓浜ч樁娈碉紝杩欎簺鏃ュ織搴旈檷绾т负 `LOGGER.debug()` 鎴栫洿鎺ョЩ闄ゃ€?

## L1. `PrototypeEvents.java` 鈥?~30 澶?INFO 鏃ュ織

| 琛屽彿 | 鍓嶇紑 | 璇存槑 |
|------|------|------|
| 261 | `D START` | 姣?tick |
| 281 | `PROTO_DIAG START` | 姣?tick |
| 287, 292 | `Prototype ground takeoff/mid-air` | 瑙﹀彂鏃?|
| 303 | `D POST_TRAVEL` | 姣?tick |
| 331, 342 | `SprintTick / SprintMod AFTER` | 姣?tick |
| 369, 377, 389 | `PROTO_DIAG CJ_CLEAR/INJECT` / `D END` | 钃勫姏鏃舵瘡 tick |
| 427, 429, 455 | `Prototype wall-run LIFT/START/CORNER` | 澧欒窇鏃?|
| 562, 582, 593 | `Sprint inertia / PROTO_DIAG` | 钃勫姏鏃?|
| 694, 735-763 | `SlamImpact / AoE` | Slam 鏃?|
| 881, 944-976 | `AoE particle / ring[]` | Slam 鏃?|

## L2. `PrototypePlayerMixin.java` 鈥?4 澶勬瘡 tick INFO

- 琛?23-24: `MIXIN_AISTEP_HEAD` 鈥?姣忎釜鐜╁姣?tick
- 琛?29-30: `MIXIN_AISTEP_RETURN` 鈥?姣忎釜鐜╁姣?tick
- 琛?37-38, 43-44: `Prototype Mixin: cancelled jump`

## L3. `PrototypeRenderEvents.java` 鈥?姣忓抚鏃ュ織

- 琛?213-218: `CLIENT_CAMERA: rollDeg=...`
- 琛?335-337: `CLIENT_WALL: sprint=...`

## L4. `RemoteEngineBlockEntity.java` 鈥?7 澶?`[TEST]` 鍓嶇紑

- 琛?56-169: `LoggerFactory.getLogger("TEST")`锛岃緭鍑?`[TEST] RE{} INIT/REFRESH/RPM/OSCILLATION/STRESS/LINK/GEN`
- **棰濆闂**: Logger 鍚嶇О `"TEST"` 鈥?濡傛灉 logback.xml 鎸?Logger 鍚嶈繃婊わ紝杩欎簺鏃ュ織鍙兘鏃犳硶琚粺涓€鎺у埗銆?

## L5. `StressReaderBlockEntity.java` 鈥?3 澶?`[TEST]` 鍓嶇紑

- 琛?75-110: 鍚屾牱 `LoggerFactory.getLogger("TEST")`

## L6. `CrushingWheelEntity.java` 鈥?~10 澶?`[WheelTest]` 鍓嶇紑

- 琛?55-163: `LOGGER.info("[WheelTest] ...")` + `LOGGER.debug(...)`銆傛瘡 tick 杈撳嚭銆?

## L7. `CrushingWheelSpell.java` 鈥?1 澶?

- 琛?131-132: `LOGGER.info("StressFocus spell 鈫?...")`

## L8. `ModClientEvents.java` 鈥?绾у埆閿欒

- 琛?43: `LOGGER.error("[MOD_CLIENT] onClientSetup called")` 鈥?姝ｅ父鍒濆鍖栧簲浣跨敤 info
- 琛?61: `LOGGER.error("[MOD_CLIENT] Flywheel Visuals DISABLED")` 鈥?姝ｅ父璺緞搴斾娇鐢?info

## L9. `ModClientForgeEvents.java` 鈥?3 澶?

- 琛?58: `LOGGER.info("X released...")`
- 琛?79: `LOGGER.info("freezePacketDelay reached 0...")`
- 琛?133: `LOGGER.info("onScreenClosed for {}")`

---

# 馃煛 閫昏緫缂洪櫡

## B1. `CrazedIllagerEntity` 鈥?AI 鐩爣浼樺厛绾у啿绐?

- **鏂囦欢**: `CrazedIllagerEntity.java:77-78`
- **闂**: 涓や釜 `NearestAttackableTargetGoal` 閮借涓轰紭鍏堢骇 `3` 鈫?闈炵‘瀹氭€ц涓?
- **淇**: 鏀逛负 `3` 鍜?`4`銆?

## B2. `CrazedSummonSpell` / `SquadSpell` 鈥?鍏ㄩ噺瀹炰綋閬嶅巻

- **鏂囦欢**: `CrazedSummonSpell.java:76`, `SquadSpell.java:91`
- **闂**: `worldIn.getAllEntities()` 閬嶅巻涓栫晫鎵€鏈夊疄浣撱€傚簲褰撳湪 `AABB` 鍐呯敤 `getEntitiesOfClass()` 杩囨护銆?
- **褰卞搷**: 澶у瀷涓栫晫锛堝鐜╁/澶氬疄浣擄級鎬ц兘寮€閿€鏄捐憲銆?

## B3. `TimeStopUtils.java` 鈥?鍏ㄩ噺 LivingEntity 閬嶅巻

- **鏂囦欢**: `TimeStopUtils.java:55-56`
- **闂**: `AABB(0,0,0).inflate(30000000)` 閬嶅巻鎵€鏈夊姞杞芥椿浣撳疄浣撱€傛瘡娆℃椂鍋滄搷浣滆Е鍙戙€?
- **寤鸿**: 鏀圭敤璺熻釜鍒楄〃锛坄TimeStopSavedData` 鎵╁睍璁℃暟鍣級鏇夸唬鍏ㄩ噺鎵弿銆?

## B4. `FleshCasketItem.getShareTag()` 鈥?淇敼婧?NBT

- **鏂囦欢**: `FleshCasketItem.java:99`
- **闂**: `getShareTag()` 灏?`cap` 閿啓鍏ョ墿鍝佹爤 NBT銆俫etter 涓嶅簲鏈夊壇浣滅敤銆?
- **淇**: `stack.getTag().copy()` 鍒涘缓鍓湰鎿嶄綔銆?

## B5. `FleshCasketItemCapability.deserializeNBT()` 鈥?鏃犵被鍨嬫鏌?

- **鏂囦欢**: `FleshCasketItemCapability.java:48`
- **闂**: `(CompoundTag) nbt` 鏃?`instanceof` 妫€鏌ャ€?
- **淇**: `if (nbt instanceof CompoundTag tag) { ... }`銆?

## B6. `FleshFurnaceBlockEntity.java:84` 鈥?`Math.abs(Integer.MIN_VALUE)` 婧㈠嚭

- **鏂囦欢**: `FleshFurnaceBlockEntity.java:84`
- **闂**: `Math.abs(Integer.MIN_VALUE)` 杩斿洖璐熸暟銆傛瀬缃曡鎯呭喌涓嬬啍鐐夋壂鎻忔案涓嶈Е鍙戙€?
- **淇**: `Math.floorMod(worldPosition.hashCode(), 60)`銆?

## B7. `RiftWalkEvents.java` 鈥?缂哄皯绌哄瓧绗︿覆瀹堝崼

- **鏂囦欢**: `RiftWalkEvents.java:52`
- **闂**: `data.getString(TAG_ORIGIN_DIM)` 杩斿洖 `""` 鏃?`new ResourceLocation("")` 鎶涘嚭寮傚父銆?
- **淇**: 妫€鏌?`isEmpty()`銆?

## B8. `C2STimeStopPacket.java` 鈥?纭紪鐮?180 tick

- **鏂囦欢**: `C2STimeStopPacket.java:28`
- **闂**: 纭紪鐮?`180` 鑰岄潪 `ModConfig.TIME_STOP_DEFAULT_DURATION.get()`
- **淇**: 浣跨敤閰嶇疆鍊笺€?

---

# 馃煛 鎬ц兘闂

## P1. `PrototypeTravelMixin` 鈥?涓?injector 閲嶅瀹堝崼妫€鏌?

- **鏂囦欢**: `PrototypeTravelMixin.java:22, 70, 131`
- **闂**: 涓変釜 `@Inject` 鍚勮嚜鐙珛璋?`PrototypeItem.hasAnyPrototype(player)`
- **寤鸿**: 鍚堝苟涓哄崟涓墠缃?injector 缂撳瓨缁撴灉銆?

## P2. `SquadFocus.findAllBoundFoci()` 鈥?鍏ㄩ噺鑳屽寘閬嶅巻

- **鏂囦欢**: `SquadFocus.java:120-130`
- **闂**: 鏃犵紦瀛樸€傞珮鐜╁鏁板満鏅瘡娆?GUI 瑙﹀彂 O(n脳m) 鎵弿銆?
- **寤鸿**: 寮曞叆 `InventoryChangeEvent` 鐩戝惉銆?

## P3. `GoetyMechJeiPlugin` 鈥?鍏ㄩ噺鐗╁搧閬嶅巻

- **鏂囦欢**: `GoetyMechJeiPlugin.java:40-43`
- **闂**: `BuiltInRegistries.ITEM.stream()` 鍚姩鏃跺叏閲忛亶鍘嗐€傞泦鎴愬寘鐗╁搧澶氭椂鐭殏鍗￠】銆?

---

# 馃數 浠ｇ爜寮傚懗

## S1. 鏈娇鐢ㄥ鍏?

| 鏂囦欢 | 鏈敤瀵煎叆 |
|------|---------|
| `MatureEmbryoItem.java:3` | `ModEntityType` |
| `FleshCasketEvents.java:14-15` | `SpellStat`, `Optional` |
| `CrazedIllagerEntity.java:3, 36` | `BlockPos`, `BlockState` |
| `SquadBarracksMenu.java:8-9` | `Player` 閲嶅 |

## S2. 鍐椾綑瑕嗗啓

| 鏂囦欢 | 闂 |
|------|------|
| `RemoteEngineBlockItem.java:68-70` | `use()` 涓庣埗绫诲畬鍏ㄤ竴鑷?|
| `LevelExpandedContext.java:14-15` | `tickHead()` 绌烘柟娉曚綋 |

## S3. 鍏ㄩ檺瀹氱被鍚嶄唬鏇?Import

| 鏂囦欢 | 琛?| 鍐呭 |
|------|----|------|
| `CuriosHelper.java:23` | `com.GoetyMech.addon.common.item.PrototypeMimicItem` | 瀹屽叏闄愬畾 |
| `MimicCombinationRecipe.java:40, 64` | `com.GoetyMech.addon.common.item.PrototypeItem` | 鏂囦欢椤堕儴宸叉湁 import |

## S4. `VoidBlinkSpell.java:84-86` 鈥?闈欓粯澶辫触

- **闂**: 鑼冨洿澶栨棤鐩爣鏃舵硶鏈秷鑰楃伒榄備絾涓嶈繑鍥炰换浣曠帺瀹跺弽棣堛€?
- **寤鸿**: `displayClientMessage()` 鎻愮ず銆?

## S5. `MixinWallRunTravel / CollectingNeighborUpdaterMixin` 鈥?閫氶厤绗︾増鏈吋瀹?

- **闂**: `@Inject(method = "neighborChanged*")` 閫氶厤绗︿粎 Mixin 0.8.2+ 鏀寔锛屼絾 `minVersion` 涓?`"0.8"`銆?
- **褰卞搷**: 0.8.0/0.8.1 鐢ㄦ埛闈欓粯澶辨晥銆?

---

# 鏁寸悊鍚庣殑琛屽姩浼樺厛绾?

## 绔嬪嵆淇

| ID | 鏂囦欢 | 闂 | 宸ヤ綔閲?|
|----|------|------|--------|
| C1 | `FleshFurnaceBlockEntity.java:70,584` | 鏁扮粍瓒婄晫 + 鏂瑰悜閿欎贡 | 5 鍒嗛挓 |
| C2 | `TimeContext.java:15` | 缂?`@OnlyIn(Dist.CLIENT)` | 5 鍒嗛挓 |

## 蹇呴』淇

| ID | 闂 | 宸ヤ綔閲?|
|----|------|--------|
| L1-L9 | ~40 澶勬畫鐣欒皟璇曟棩蹇?鈫?闄嶇骇 `debug` 鎴栫Щ闄?| 30 鍒嗛挓 |
| B2, B3 | 鍏ㄩ噺瀹炰綋閬嶅巻 鈫?`getEntitiesOfClass` / 璺熻釜鍒楄〃 | 30 鍒嗛挓 |
| B4, B5 | `getShareTag` 鍓綔鐢?+ `deserializeNBT` 鏃?instanceof | 15 鍒嗛挓 |

## 闇€楠岃瘉鍚庝慨澶?

| ID | 闂 | 闇€楠岃瘉 |
|----|------|--------|
| G2 | `MinionSpawnHelper` 鎶樻墸閫昏緫 | Goety `SEHelper.decreaseSouls` 鏄惁鍐呴儴搴旂敤 `soulDiscount` |
| G3 | `TimeStopUtils` 瑙ｅ喕閫昏緫 | EndLibrary 鍘熷琛屼负 / 椤圭洰璁捐鎰忓浘 |

## 娓愯繘鏀硅繘

| ID | 闂 | 绫诲瀷 |
|----|------|------|
| P1-P3 | 鐑矾寰勬棤缂撳瓨 / 鍏ㄩ噺閬嶅巻 | 鎬ц兘 |
| S1-S5 | 鏈敤瀵煎叆 / 鍐椾綑 / 妗╀唬鐮?| 浠ｇ爜璐ㄩ噺 |
