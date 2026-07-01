# 鍘熷瀷 (Prototype) 鏋舵瀯璇存槑

## 姒傝堪

鍘熷瀷鏄熀浜?Goety 妯＄粍鐨勯灅缈呯被鐗╁搧锛屾敮鎸?**鑳哥敳妲?* 鍜?**Curios 鑳岄儴/韬綋妲?*銆傛牳蹇冪帺娉曚负 **椋炶鈫掔寷鍑烩啋浜屾寤惰繜榄旀硶浼ゅ** 鍙岄樁娈靛惊鐜紝闄勫甫 **鐖绯荤粺**銆?*璺宠穬钃勫姏** 鍜?**鍦伴潰鍐插埡鍔犻€?*銆?

## 鏂囦欢缁撴瀯

```
src/main/java/com/GoetyMech/addon/
鈹溾攢鈹€ common/
鈹?  鈹溾攢鈹€ item/
鈹?  鈹?  鈹溾攢鈹€ PrototypeItem.java              # 鐗╁搧瀹氫箟 + PersistentData tag 闈欐€佽闂櫒
鈹?  鈹?  鈹斺攢鈹€ curio/PrototypeCurio.java       # Curios 鑳藉姏闄勫姞鍣紙curioTick / canEquip锛?
鈹?  鈹溾攢鈹€ events/
鈹?  鈹?  鈹斺攢鈹€ PrototypeEvents.java            # 鏍稿績閫昏緫锛歴lam 鐘舵€佹満銆佺埇澧欍€佽搫鍔涖€佺幆鍔ㄧ敾銆佷激瀹?
鈹?  鈹溾攢鈹€ utils/
鈹?  鈹?  鈹溾攢鈹€ CuriosHelper.java               # Curios 妲戒綅妫€娴嬪伐鍏?
鈹?  鈹?  鈹斺攢鈹€ WallUtil.java                   # 鐖鍑犱綍宸ュ叿锛堝闈㈡娴嬨€佸悜閲忔姇褰憋級
鈹?  鈹溾攢鈹€ net/
鈹?  鈹?  鈹溾攢鈹€ ModNetwork.java                 # 缃戠粶閫氶亾娉ㄥ唽
鈹?  鈹?  鈹溾攢鈹€ c2s/C2SCancelSlamPacket.java    # 瀹㈡埛绔啋鏈嶅姟绔細鍙栨秷鐚涘嚮
鈹?  鈹?  鈹溾攢鈹€ c2s/C2SJumpChargePacket.java    # 瀹㈡埛绔啋鏈嶅姟绔細璺宠穬钃勫姏 tick
鈹?  鈹?  鈹溾攢鈹€ c2s/C2SFlightInputPacket.java   # 瀹㈡埛绔啋鏈嶅姟绔細椋炶 W 閿緭鍏?(forward 0/1)
鈹?  鈹?  鈹斺攢鈹€ s2c/S2CSlamImpactPacket.java    # 鏈嶅姟绔啋瀹㈡埛绔細鍐插嚮绮掑瓙+闊虫晥
鈹?  鈹斺攢鈹€ config/ModConfig.java               # 鍏ㄩ儴閰嶇疆椤?
鈹溾攢鈹€ client/
鈹?  鈹斺攢鈹€ PrototypeRenderEvents.java          # 瀹㈡埛绔細缈呯考娓叉煋銆佺埇澧欓暅澶?妯″瀷鍊炬枩銆佹寜閿娴?
鈹溾攢鈹€ compat/caelus/
鈹?  鈹斺攢鈹€ CaelusFlightHandler.java            # Caelus 鍏煎锛欳urios 鑳岄儴妲芥椂娣诲姞椋炶 attribute
鈹斺攢鈹€ mixin/
    鈹溾攢鈹€ PrototypeTravelMixin.java           # Player.travel() 鍙屾敞鍏ワ細椋炶 velocity-set + TAIL 婕傜Щ
    鈹斺攢鈹€ PrototypePlayerMixin.java           # 闃绘钃勫姏鏃惰Е鍙?Player.jumpFromGround()
```

## 鏍稿績鏋舵瀯

### 1. 鐗╁搧瀹氫箟

`PrototypeItem extends ElytraItem`锛?

- `canElytraFly()` 鈫?鎭?true
- `elytraFlightTick()` 鈫?slam 鏃惰繑鍥?false锛堝己鍒跺仠椋烇級锛屽惁鍒?true
- `isEnchantable()` 鈫?false锛堜笉鍙檮榄旓級
- **鎵€鏈夌姸鎬佸瓨浜?entity PersistentData**锛岃€岄潪鐗╁搧 NBT

#### 鐘舵€?tag 鎬昏锛堝叏閮ㄤ负闈欐€佹柟娉曪紝閫氳繃 entity 璇诲啓锛?

| Tag | 璁块棶鍣?| 鐢ㄩ€?|
|-----|--------|------|
| `GoetyMech.slamming` | `isSlamming/setSlamming` | 鐚涘嚮鐘舵€佹爣蹇?|
| `GoetyMech.slam_tick` | `getSlamTick/setSlamTick` | 鐚涘嚮宸叉寔缁?tick 鏁?|
| `GoetyMech.slam_start_y` | `getSlamStartY/setSlamStartY` | 鐚涘嚮璧峰 Y 鍧愭爣 |
| `GoetyMech.prev_crouch` | `getPrevCrouch/setPrevCrouch` | 涓婁竴 tick 鐨?crouch 鐘舵€侊紙涓婂崌娌挎娴嬬敤锛?|
| `GoetyMech.slam_invuln_tick` | `getSlamInvulnTick/setSlamInvulnTick` | 鐚涘嚮鏃犳晫甯у墿浣欐椂闂达紙鐚涘嚮鍏ㄧ▼ + 钀藉湴鍚?20 tick = 1 绉掞級 |
| `GoetyMech.wall_running` | `isWallRunning/setWallRunning` | 鐖鐘舵€佹爣蹇?|
| `GoetyMech.wall_nx/ny/nz` | `getWallNormal/setWallNormal` | 澧欓潰娉曠嚎鏂瑰悜 |
| `GoetyMech.wall_run_tick` | `getWallRunTick/setWallRunTick` | 鐖宸叉寔缁?tick 鏁?|
| `GoetyMech.jump_charge_tick` | `getJumpChargeTick/setJumpChargeTick` | 璺宠穬钃勫姏 tick 鏁?|
| `GoetyMech.sprint_tick` | `getSprintTick/setSprintTick` | 鍦伴潰鍐插埡宸叉寔缁?tick 鏁?|
| `GoetyMech.flight_forward` | `getFlightForward/setFlightForward` | 椋炶 W 閿緭鍏ョ姸鎬侊紙0 鎴?1锛?|

#### 瑁呭妫€娴嬶紙澶氬浣跨敤锛?

`isHoldingPrototype(entity)` 鈥?浠呮娴?`EquipmentSlot.CHEST`
`hasPrototypeInCurios(player)` 鈥?浠呮娴?Curios 浠绘剰妲戒綅锛坱ry-catch 鍖呰９闃叉 Curios 缂哄け NPE锛?
`hasAnyPrototype(entity)` 鈥?鍚屾椂妫€娴嬭兏鐢叉Ы + Curios 妲?

### 2. 浜嬩欢鐩戝惉鍣紙Forge 鎬荤嚎锛?

鍏ㄩ儴鍦?`PrototypeEvents` 涓€氳繃 `@SubscribeEvent` 娉ㄥ唽锛坄Mod.EventBusSubscriber` + `Bus.FORGE`锛夛細

| 浜嬩欢 | Phase | 鐢ㄩ€?|
|------|-------|------|
| `LivingFallEvent` | 鈥?| 鎸佹湁鍘熷瀷鎴栨鍦?slam 鏃跺彇娑堝潬钀戒激瀹?|
| `LivingAttackEvent` | 鈥?| `slam_invuln_tick > 0` 鏃跺厤鐤墍鏈変激瀹?|
| `LivingEvent.LivingTickEvent` | 鈥?| slam 鐘舵€佹満锛堣Е鍙?涓嬭惤锛夛紱slam invuln 閫掑噺锛沺revCrouch 璁板綍 |
| `TickEvent.PlayerTickEvent` | START | 鐖妫€娴?鐘舵€佹洿鏂般€佸啿鍒哄姞閫熷睘鎬т慨楗板櫒绠＄悊 |
| `TickEvent.PlayerTickEvent` | END | 鐖杩愬姩瀛﹀簲鐢?`applyWallMovement` |
| `TickEvent.LevelTickEvent` | END | 鐜姩鐢婚€?tick 鎺ㄨ繘銆佸欢杩熶激瀹冲埌鏈熻Е鍙?|
| `LevelEvent.Unload` | 鈥?| 娓呯悊 `ACTIVE_SLAMS` / `PENDING_DAMAGES` |

**鍏抽敭鏃跺簭**锛歴lam 鐘舵€佹満鍦?`LivingTickEvent` 涓鐞嗭紙鍏堜簬 `updateFallFlying`锛夛紝鍙悓 tick 鍋滈銆傜埇澧欒繍鍔ㄥ鍦?`PlayerTickEvent.END` 涓簲鐢紙鍏堜簬绉诲姩缁撶畻锛夈€?

### 3. 鍙岄樁娈靛惊鐜?

```
椋炶闃舵                                   鐚涘嚮闃舵
鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?   shift 鎸変笅             鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?
鈹?Elytra Flight 鈹?鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈫?鈹? Hover (SLAM_HOVER_TICKS) 鈹?
鈹?(鏃犻澶栧姞閫?   鈹?                         鈹? 姘村钩脳0.9鍑忛€燂紝鍨傜洿鎮仠    鈹?
鈹?浠呮姉 drag    鈹?                         鈹? slam_tick++              鈹?
鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?                         鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?
       鈫?                                          鈹?hover 缁撴潫
       鈹?                                          鈫?
       鈹?                                  鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?
       鈹?  jump 鎸変笅锛堝彇娑堬級                鈹? Descend (鍔犻€熶笅钀? 鈹?
       鈹?  鈫愨攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€    鈹? V(t) = 0.15 + gravity脳t 鈹?
       鈹?                                  鈹? 涓婇檺 SLAM_MAX_SPEED     鈹?
       鈹?                                  鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?
       鈹?                                           鈹?onGround() || inFluid()
       鈹?                                           鈫?
       鈹?                                  鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?
       鈹?                                  鈹? Impact (鍐插嚮)    鈹?
       鈹?                                  鈹? 鈹?AoE 瀹炰綋浼ゅ   鈹?
       鈹?                                  鈹? 鈹?鏂瑰潡鐜崌璧峰姩鐢? 鈹?
       鈹?                                  鈹? 鈹?绾㈣壊 AoE 鍦嗗舰绮掑瓙鈹?
       鈹?                                  鈹? 鈹?闀滃ご闇囧姩       鈹?
       鈹?                                  鈹? 鈹?鐖嗙偢闊虫晥       鈹?
       鈹?                                  鈹? 鈹?鍙戦€?S2C 鍖?  鈹?
       鈹?                                  鈹? 鈹?璁剧疆 invuln=20 tick 鈹?
       鈹?                                  鈹? 鈹?璋冨害 60tick 鍚?鈹?
       鈹?                                  鈹?   浜屾榄旀硶浼ゅ   鈹?
       鈹?                                  鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?
```

#### 3.1 杩涘叆鐚涘嚮

`handleSlamTrigger()`锛氭娴?`nowCrouch && !prevCrouch`锛坰hift 閿笂鍗囨部锛夆啋 `enterSlamState()`锛?
- 璁剧疆 `slamming=true`銆乣slam_tick=0`銆乣slam_start_y=player.getY()`
- `elytraFlightTick()` 杩斿洖 false 鈫?鍚?tick 鍋滈
- 璁剧疆鍒濆涓嬮€熷害 `-0.15`
- **璁剧疆 invuln=9999**锛堢寷鍑诲叏绋嬫棤鏁岋級
- 鎾斁鐏鍙戝皠闊虫晥

#### 3.2 鐚涘嚮涓嬭惤

`handleSlamTick()`锛?
- `tick < SLAM_HOVER_TICKS`锛氭按骞?脳0.9 鍑忛€燂紝鍨傜洿鎮仠锛堟粸绌猴級
- `tick >= SLAM_HOVER_TICKS`锛歚閫熷害 = 0.15 + gravity_per_tick 脳 (tick - hover_ticks)`锛屼笂闄?SLAM_MAX_SPEED
- 寮哄埗鍏抽棴鍒涢€犻琛?

#### 3.3 鍐插嚮

`triggerSlamImpact()`锛?

```
fallDistance = max(0, slamStartY - player.getY())

damage = clamp(fallDistance 脳 SLAM_DAMAGE_MULTIPLIER,
               SLAM_MIN_DAMAGE, SLAM_MAX_DAMAGE)

radius = clamp(fallDistance 脳 SLAM_RADIUS_MULTIPLIER,
               8.0, SLAM_MAX_RADIUS)

rings  = min(4 + max(0, (fallDist-10)/20), 9, maxRingsByRadius)
       鍏朵腑 maxRingsByRadius = max(0, floor(radius)-5)
```

- AoE 浼ゅ锛欰ABB `[x卤radius, groundY-2 ~ groundY+3, z卤radius]`锛宍DamageSource.playerAttack(player)`锛屽潎鍖€鍒嗗竷鏃犺“鍑?
- 姣忔鍛戒腑锛氬悜澶栧嚮椋烇紙姘村钩 2.5 + 鍨傜洿 0.5~0.8锛?
- **璁剧疆 slam_invuln_tick=20**锛堣惤鍦板悗 1 绉掓棤鏁岋紝閫氳繃 `LivingAttackEvent` 瀹炵幇锛?
- 绔嬪嵆娓呴櫎 slam 鐘舵€?
- 璋冨害 `PENDING_DAMAGES`锛?0 tick 鍚庝簩娆′激瀹筹級

### 4. 浜屾寤惰繜浼ゅ锛圖elayed Damage锛?

| 灞炴€?| 鍊?|
|------|-----|
| 瑙﹀彂鏃舵満 | 棣栨鍐插嚮鍚?60 ticks锛? 绉掞級 |
| 浼ゅ閲?| 棣栨浼ゅ 脳 50% |
| 浼ゅ绫诲瀷 | `indirectMagic(player, player)` 鈥?鏃犺鎶ょ敳 |
| 鐗规晥 | 鐧借壊 `CircleExplodeParticleOption` 鎵╁紶鐜?+ `END_PORTAL_SPAWN` 闊虫晥 |
| 鍑婚 | 姘村钩 3.0 + 鍨傜洿 1.0~1.5 |

瀹炵幇锛歚PENDING_DAMAGES: Map<ServerLevel, List<DelayedDamageData>>`锛宍onLevelTick` 姣?tick 妫€鏌?`gameTime >= triggerTick`銆?

### 5. 鐜姩鐢荤郴缁燂紙Ring Animation锛?

鍩轰簬 Goety `ModFallingBlock` 瀹炰綋锛岄€愬湀鎵╂暎鏂瑰潡鍔ㄧ敾銆?

```
ACTIVE_SLAMS: Map<ServerLevel, List<SlamAnimData>>
```

**`SlamAnimData`**锛歚center` / `floorY` / `radius` / `totalRings` / `nextRingToSpawn` / `animTick` / `blocks` / `pendingBlocks` / `allRingsSpawned`

**姣?tick 鎺ㄨ繘**锛坄onLevelTick` END锛夛細
1. `animTick++`
2. 浠?`pendingBlocks` 閲婃斁鍒版湡鐨勫欢杩熸柟鍧?
3. 鑻?`animTick >= nextRingToSpawn 脳 RINGS_DELAY(1)` 鈫?`spawnSingleRing()`
4. 绉婚櫎宸叉浜＄殑 `ModFallingBlock`
5. 鎵€鏈?ring 鐢熸垚瀹屾瘯 + 鏃犳椿璺?寰呭鐞嗘柟鍧?鈫?绉婚櫎璇?`SlamAnimData`

**鐜敓鎴?*锛坄spawnSingleRing`锛夛細
- 鐜捣濮嬪崐寰?= `5.0 + ringIdx`锛屽搴?1 鏍?
- 閫氳繃 `findGroundPos` 鎵惧埌鍦伴潰鏂瑰潡锛堝綋鍓嶆牸 + 鍚戜笅 3 鏍硷級
- **闈炴渶澶栫幆** 鈫?`POPUP_ANIM` 妯″紡锛歚popUp = sqrt(desiredHeight 脳 0.2)`锛岄殢鏈哄欢杩?0-7 tick 閿欏紑
- **鏈€澶栫幆** 鈫?`MOBILE` 妯″紡锛氬悜澶栭鏁?+ 鏃嬭浆 + 閲嶅姏

#### 鍚屾瑙嗚锛坄spawnVisuals`锛屽啿鍑绘椂瑙﹀彂锛?

| 瑙嗚 | 瀹炵幇 |
|------|------|
| 鏂瑰潡鍗囪捣鐜?| `startRingAnimation`锛堥€愬湀閿欏紑锛?|
| MOBILE 椋炴暎纰庡潡 | 20 涓?`ModFallingBlock(MOBILE)`锛屾按骞?3 鏍煎唴闅忔満 |
| 涓績 DEBRIS | 15 涓?`ModFallingBlock(MOBILE)`锛?.5~2.0 鏍煎悜澶?|
| 绾㈣壊澶栧湀杈圭晫 | `CircleExplodeParticleOption(1,0,0, radius, 2)` 鈥?鍗虫椂鐖嗗彂 |
| 绾㈣壊 AoE 鍦嗗舰 | `AoEParticleOption(radius, 60)` 鈥?鎸佺画 3 绉掑崐閫忔槑绾㈠湀 |
| 闀滃ご闇囧姩 | `CameraShake.cameraShake(radius脳1.5, 0.15, 15, 10)` |
| 鐖嗙偢闊虫晥 | 鏈嶅姟绔?+ 瀹㈡埛绔紙S2C 鍖咃級 |

### 6. 鐖绯荤粺锛圵all-Running锛?

#### 6.1 澧欓潰妫€娴嬶紙`WallUtil.getWallDirection`锛?

鍙栫帺瀹朵袱涓?AABB 閲囨牱鐩掞紙鑴氶儴 30% + 澶撮《 15% 楂樺害锛夛紝鍚?卤X/卤Z 鍚勬墿灞?`range=0.5`锛屾娴嬫槸鍚︿笌鏂瑰潡纰版挒锛?

```
鑴氶儴鐩掑瓙: (x卤width, y, z卤width) 鈫?(x卤width, y+height脳0.3, z卤width)
澶撮《鐩掑瓙: (x卤width, y+height脳0.85, z卤width) 鈫?(x卤width, y+height, z卤width)
```

杩斿洖娉曠嚎鍚戦噺 `Vec3(wallX, 0, wallZ)` 鎴?null銆?

#### 6.2 杩涘叆鐖

`tryStartWallRun()`锛圥layerTick START锛夛細
- 鏉′欢锛氭寔鏈夊師鍨?+ 闈?slam + 闈炲湴闈?+ 闈炴恫浣?+ 鏈夊闈?
- 璁剧疆 `wall_running=true`銆乣wall_normal`銆乣wall_run_tick=0`
- 鑻ユ鍦ㄩ琛?鈫?`stopFallFlying()`
- **缁ф壙閫熷害**锛氬垎瑙ｄ负娌垮闈㈠垎閲?+ 鍏ュ闈㈠垎閲忥紝鍘绘帀鍏ュ鍒嗛噺淇濈暀鍒囧悜閫熷害

#### 6.3 缁存寔鐖

`handleWallRunTick()`锛圥layerTick START锛夛細
- 姣?tick 妫€娴嬶細鑻?shift/娑蹭綋/鍦伴潰/椋炶/澧欏娑堝け 鈫?`stopWallRun()`
- `wall_run_tick++`

#### 6.4 鐖杩愬姩瀛?

`applyWallMovement()`锛圥layerTick END锛夛細

```
surfaceVel = 褰撳墠閫熷害鎶曞奖鍒板闈㈠垏骞抽潰
forwardOnWall = 瑙嗙嚎鏂瑰悜鎶曞奖鍒板闈㈠垏骞抽潰锛堝崟浣嶅悜閲忥級
rightOnWall = forwardOnWall 脳 wallNormal锛堝崟浣嶅悜閲忥級

inputAccel = forwardOnWall 脳 zza 脳 baseAccel
           + rightOnWall 脳 strafe 脳 baseAccel
           鍏朵腑 baseAccel = MOVEMENT_SPEED 脳 0.3

surfaceVel = (surfaceVel + inputAccel) 脳 WALL_FRICTION(0.98)
surfaceVel += (0, -WALL_SLIDE_SPEED(0.005), 0)  # 鏋佽交寰笅婊?

鏈€缁堥€熷害 = surfaceVel + wallNormal 脳 WALL_ADHESION(0.05)
```

#### 6.5 瀹㈡埛绔瑙夋晥鏋?

`PrototypeRenderEvents`锛?
- **Camera 60掳 roll**锛坄ViewportEvent.ComputeCameraAngles`锛夛細鏍规嵁澧欓潰娉曠嚎鐩稿瑙嗚鍙虫柟鍚戯紝骞虫粦杩囨浮鍊炬枩
- **Model 60掳 tilt**锛坄RenderPlayerEvent.Pre`锛夛細鐜╁ body/head/arms/legs 鍚屾 `zRot` 鍊炬枩
- 骞虫粦鎻掑€硷細`clientWallRoll += diff 脳 0.15`

### 7. 璺宠穬钃勫姏锛圝ump Charge锛?

#### 7.1 瀹㈡埛绔娴?

`onClientTick`锛坄PrototypeRenderEvents`锛夛細
- `jumpDown && hasPrototype && !isFallFlying && (onGround || wallRunning)`
- 钃勫姏鏈熼棿姣?2 tick 鍙戦€?`C2SJumpChargePacket(tick)`锛堝噺灏戠綉缁滈噺锛?
- 绮掑瓙鍙嶉锛氭瘡 3 tick 鐢熸垚 `CRIT` 绮掑瓙锛涜秴杩?40 tick 棰濆鐢熸垚 `ENCHANTED_HIT` 绮掑瓙
- jump 鏉惧紑鏃跺彂閫?`C2SJumpChargePacket(0)` 瑙﹀彂璺宠穬

#### 7.2 鏈嶅姟绔鐞?

`handleJumpChargeFromClient()`锛?
- `action > 0`锛氬瓨鍌ㄨ搫鍔?tick锛堜笂闄?`JUMP_CHARGE_MAX_TICKS`锛?
- `action == 0`锛氭墽琛岃搫鍔涜烦璺?

#### 7.3 璺宠穬楂樺害鍏紡

```
threshold = JUMP_THRESHOLD_TICKS(20)
if charge < threshold:
    height = JUMP_SMALL_HEIGHT(3.0)
else:
    t = (charge - threshold) / (maxTicks - threshold)
    height = smallHeight + initialBonus + t 脳 (maxHeight - smallHeight - initialBonus)

jumpPower = sqrt(2 脳 0.08 脳 height)
```

| 閰嶇疆 | 榛樿鍊?| 璇存槑 |
|------|--------|------|
| `jump_threshold_ticks` | 20 | 钃勫姏闃堝€硷紝灏忎簬姝ゅ€艰Е鍙戝皬璺?|
| `jump_small_height` | 3.0 | 灏忚烦楂樺害锛堟牸锛?|
| `jump_charge_initial_bonus` | 5.0 | 钃勫姏鍒濆棰濆楂樺害 |
| `jump_charge_max_height` | 30.0 | 钃勫姏婊℃椂鏈€澶ч珮搴?|
| `jump_charge_max_ticks` | 60 | 鏈€闀胯搫鍔涙椂闂达紙3 绉掞級 |

#### 7.4 Mixin 闃绘鏅€氳烦璺?

`PrototypePlayerMixin` 鈫?`Player.jumpFromGround()` HEAD 娉ㄥ叆锛?
- `jumpChargeTick > 0` 鈫?cancel锛堥槻姝㈣搫鍔涙椂鎸夌┖鏍艰Е鍙戣烦璺冿級
- `jumping && 鑳哥敳妲芥湁鍘熷瀷` 鈫?cancel锛堥槻姝㈤琛屾椂璇Е锛?

### 8. 鍐插埡鍔犻€燂紙Sprint Acceleration锛?

`onPlayerTick` START phase锛?
- 鏉′欢锛氭寔鏈夊師鍨?+ `isSprinting()` + 鍦伴潰 + `zza > 0` + 闈?slam + 闈?wall-run + 闈為琛?
- `sprint_tick++`锛宍level = min(sprint_tick / 20, 3)`
- 姣?tick 鏇挎崲 `MOVEMENT_SPEED` attribute锛歚multiply_total 脳 (0.6 + level 脳 0.2)`
- sprint 鍋滄鏃舵竻闄?modifier

### 9. 鐚涘嚮鏃犳晫甯?

瀹炵幇涓ゅ眰淇濇姢锛?

| 淇濇姢 | 瀹炵幇 | 鎸佺画鏃堕棿 |
|------|------|---------|
| 鐚涘嚮涓?| `enterSlamState` 鏃惰缃?`invuln=9999` | 鏁翠釜鐚涘嚮杩囩▼ |
| 钀藉湴鍚?| `triggerSlamImpact` 鏃惰缃?`invuln=20` | 钀藉湴鍚?1 绉?|

閫氳繃 `LivingAttackEvent` 鐩戝惉锛歚slam_invuln_tick > 0` 鈫?`setCanceled(true)`銆?

`LivingTickEvent` 涓瘡 tick 閫掑噺 `invuln`锛堝噺鍒?0 鍚庢仮澶嶅彈鍑伙級銆?

### 10. 椋炶鍔涘

椋炶闃舵浣跨敤 Hybrid 妯″瀷锛?*鍙屾敞鍏ョ偣鏋舵瀯**锛岃鐩?W 鎸変笅锛圚EAD velocity-set锛夊拰 W 鏉惧紑锛圱AIL 鍔犳硶婕傜Щ锛夈€傚叏閮ㄥ疄鐜板湪 `PrototypeTravelMixin` 涓€?

#### 10.1 V6 Hybrid 妯″瀷

姣?tick 浠庡鎴风 `PrototypeRenderEvents.onClientTick` 鍚屾 W 閿姸鎬佸埌鏈嶅姟绔細
```
瀹㈡埛绔? 姣?tick 鍐欐湰绔?+ 姣?3 tick 鍙?C2S 鈫?C2SFlightInputPacket(forward)
鏈嶅姟绔? 瀛樺偍鑷?entity PersistentData 鈫?TAG_FLIGHT_FORWARD
```

**鍙屾敞鍏ョ偣鏋舵瀯**锛?

```
HEAD injection (cancellable=true):
  forward > 0.01 鈫?velocity-set + pitch 渚濊禆 lift + 淇啿 dot+gravity 鎶曞奖
  forward 鈮?0.01 鈫?涓嶆嫤鎴紝fall through 鍒?vanilla travel()

TAIL injection:
  forward 鈮?0.01 鈫?閫熷害 脳0.8锛屽姞娉?baseAccel锛堟按骞宠绾挎柟鍚戯級
  forward > 0.01 鈫?璺宠繃锛堝凡鐢?HEAD 澶勭悊锛?
```

**HEAD 娉ㄥ叆 鈥?velocity-set + 淇啿 dot+gravity 鎶曞奖**锛坄gp$onTravelHead`锛夛細

褰?W 鎸変笅鏃讹紝鏍规嵁淇话瑙掗€夋嫨涓ゅ鐙珛妯″瀷锛?

**姘村钩/鎶ご锛坧itch 鈮?0.01锛?*锛?
```
targetVy = look.y 脳 speed + liftFactor 脳 hoverLift
liftFactor = clamp(-pitch / 90, 0, 1)    # 浠板ご瓒婂ぇ lift 瓒婂ぇ

vMotion = (look.x 脳 speed, targetVy, look.z 脳 speed)

if newH < 0.001:          鈫?鐩磋姝ｄ笂/涓嬶紝姘村钩褰掗浂
elif currH > newH:        鈫?淇濇寔鏃ф按骞冲姩閲忓ぇ灏?
else:                     鈫?鐢?velocity-set 鏂板€?
```

**淇啿锛坧itch > 0.01锛?*锛?
```
speedAlongLook = max(speed, current路look + 0.08 脳 (-look.y))  # dot+gravity 鎶曞奖
targetMotion = look 脳 speedAlongLook

motion = current 脳 0.3 + targetMotion 脳 0.7    # 70/30 娣峰悎骞虫粦杩囨浮
```

**TAIL 娉ㄥ叆 鈥?涓嶆寜 W 鐨?0.8 鍊嶉€?*锛坄gp$onTravelTail`锛夛細

褰?W 鏉惧紑鏃讹紝鎵€鏈夐€熷害鐩爣鍊煎彇 W 鐘舵€佺殑 80%锛?

```
noWSpeed = speed 脳 0.8
noWHoverLift = hoverLift 脳 0.8

if pitch > 0.01 (淇啿):
    speedAlongLook = max(noWSpeed, current路look + 0.08 脳 (-look.y))
    targetMotion = look 脳 speedAlongLook
    velocity = current 脳 0.3 + targetMotion 脳 0.7
    姘村钩: += lookH 脳 baseAccel

else (姘村钩/鎶ご):
    targetVy = look.y 脳 noWSpeed + noWHoverLift 脳 liftFactor
    velocity = current + lookH 脳 baseAccel
    Y = targetVy
```

**鏁堟灉**锛?

| 鍦烘櫙 | 鏈哄埗 | 鎵嬫劅 |
|------|------|------|
| 鎸変綇 W 骞抽 | velocity-set `targetSpeed=0.5` | 鍗虫椂鍝嶅簲锛岀ǔ瀹氬墠椋?|
| 鎸変綇 W 鎶ご | velocity-set + lift 闅忎话瑙掔嚎鎬у鍔?| 鎶ご涓婂崌涓嶆帀姘村钩 |
| 鎸変綇 W 淇啿 | dot+gravity 鎶曞奖 + 70/30 娣峰悎 | 閫熷害娌胯绾挎柟鍚戝榻愶紝姘村钩涓嶈惤鍚?|
| 鏉惧紑 W | 鍏ㄩ€熺巼 脳0.8 + 姘村钩 drift | 娓╁拰鍚戝墠婕傜Щ锛岄€熷害浣庝簬 W 鐘舵€?|
| 鐩磋姝ｄ笂 | `newH < 0.001` 鈫?姘村钩褰掗浂 | 绾瀭鐩翠笂鍗囷紝鏃犳按骞冲亸绉?|

**鍐呴儴姣旂巼**锛堢‖缂栫爜锛屼笉鏆撮湶涓洪厤缃級锛?

| 鍊?| 璁＄畻 | 鏉ユ簮 |
|----|------|------|
| `hoverLift` | `speed 脳 0.16` | 鍏奸【 lift 涓庢儻鎬т繚鐣?|
| `baseAccel` | `speed 脳 0.04` | 鏃?W 婕傜Щ鍔犻€熷害 |
| `noWSpeed` | `speed 脳 0.8` | 涓嶆寜 W = 鎸?W 鐨?4/5 |
| `liftRatio` | `0.16` | hoverLift / speed |
| `accelRatio` | `0.04` | baseAccel / speed |

**閰嶇疆椤?*锛坄GoetyMech-common.toml` 鈫?`prototype` 娈碉級锛?

| 閰嶇疆椤?| 榛樿鍊?| 鑼冨洿 | 璇存槑 |
|--------|--------|------|------|
| `flight_target_speed` | 0.5 | 0.1~2.0 | **鍞竴閫熷害閰嶇疆**銆傞┍鍔?lift(脳0.16)銆佹紓绉?脳0.04)銆佹棤W閫熷害(脳0.8) |

**浠ｇ爜浣嶇疆**锛?
- `mixin/PrototypeTravelMixin.java` 鈥?HEAD + TAIL 鍙屾敞鍏?
- `client/PrototypeRenderEvents.java` 鈥?C2S 鍙戦€?+ 鏈 forward 鍐欏叆
- `common/item/PrototypeItem.java` 鈥?getFlightForward / setFlightForward
- `config/ModConfig.java` 鈥?FLIGHT_TARGET_SPEED
- `common/net/c2s/C2SFlightInputPacket.java` 鈥?缃戠粶浼犺緭

**涓庡叾浠栫郴缁熶簰鏂?*锛?
- Slam 鏈熼棿 `isFallFlying() = false` 鈫?Mixin return
- 鐖鏈熼棿 `PlayerTickEvent.END` 瑕嗗啓 velocity 鈫?Mixin 鏁堟灉琚鐩栵紙鏃犲锛?

### 11. 鍙栨秷鐚涘嚮

```
瀹㈡埛绔細TickEvent.ClientTickEvent.END
  鈹?妫€娴?keyJump 涓婂崌娌匡紙wasPressed锛夆啋 闈炵埇澧欐椂鍙戦€?C2SCancelSlamPacket

鏈嶅姟绔細C2SCancelSlamPacket.handle()
  鈹?鏍￠獙 isSlamming() 鈫?璁剧疆鍚戜笂閫熷害 max(0.5, currentY) 鈫?clearSlamState() 鈫?fallDistance=0
```

### 12. 瀹㈡埛绔覆鏌?

`PrototypeRenderEvents`锛?

| 浜嬩欢 | 鐢ㄩ€?|
|------|------|
| `RenderPlayerEvent.Post` (LOWEST) | 鐜╁ body 鍚庢覆鏌撶繀缈兼ā鍨嬶紙`textures/entity/prototype.png`锛夛紝鍚屾椂妫€娴?Curios 妲戒綅 |
| `ViewportEvent.ComputeCameraAngles` | 鐖鏃?camera roll 60掳 |
| `RenderPlayerEvent.Pre` | 鐖鏃?player model 鏁翠綋鍊炬枩 |
| `TickEvent.ClientTickEvent.END` | 澧欏鐘舵€佹洿鏂般€佸彇娑?slam 鍙戦€併€佽烦璺冭搫鍔?C2S 鍙戦€?|
| `S2CSlamImpactPacket` handle | 瀹㈡埛绔挱鏀?`BlockParticleOption` 闆惧寲绮掑瓙 + `GENERIC_EXPLODE` 闊虫晥 |

### 13. Curios 鍏煎

`PrototypeCurio` 鈫?`AttachCapabilitiesEvent<ItemStack>`锛?

| 鏂规硶 | 琛屼负 |
|------|------|
| `curioTick` | 鍦?Curios 妲戒腑缁х画椋炶 tick锛堣皟鐢?`elytraFlightTick`锛?|
| `canEquip` | 鏃?Caelus 鏃剁姝㈡斁鍏?Curios 妲斤紱鑳哥敳妲藉凡鏈夊師鍨嬫椂绂佹鏀惧叆鑳?鑳告Ы |
| `canEquipFromUse` | 浠?Caelus 鍔犺浇鏃跺彲鍙抽敭瑁呭 |
| `getEquipSound` | 闉樼繀瑁呭闊虫晥 |

### 14. Caelus 鍏煎

`CaelusFlightHandler` 鈫?褰撳師鍨嬪湪 Curios 鑳岄儴妲芥椂锛岄€氳繃 `caelus:flight` attribute 娣诲姞椋炶鑳藉姏锛?
- `ADDITION` 淇グ鍣紝鍊?1.0
- 姣?tick 娓呴櫎鍚庨噸鏂版坊鍔狅紙闃叉浣滃紛/鍛戒护淇敼锛?
- 閫氳繃 `ResourceLocation` 瀛楃涓叉煡璇?attribute锛岄伩鍏嶇洿鎺?import Caelus 绫?

### 15. 閰嶇疆鍙傛暟锛坄GoetyMech-common.toml`锛?

#### Prototype 娈?

| 閰嶇疆椤?| 榛樿鍊?| 鑼冨洿 | 璇存槑 |
|--------|--------|------|------|
| `slam_gravity_per_tick` | 0.25 | 0.01~2.0 | 姣?tick 涓嬭惤鍔犻€熷害 |
| `slam_max_speed` | 6.0 | 0.5~20.0 | 鏈€澶т笅钀介€熷害 |
| `slam_damage_multiplier` | 1.0 | 0.0~10.0 | 浼ゅ = 鍧犺惤楂樺害 脳 姝ゅ€?|
| `slam_radius_multiplier` | 1.0 | 0.0~20.0 | 鍗婂緞 = 鍧犺惤楂樺害 脳 姝ゅ€?|
| `slam_min_damage` | 20.0 | 1~9999 | 鏈€浣庝激瀹充繚搴?|
| `slam_max_damage` | 1200.0 | 1~9999 | 浼ゅ涓婇檺 |
| `slam_max_radius` | 100.0 | 1~200 | 鍗婂緞涓婇檺 |
| `slam_hover_ticks` | 5 | 0~100 | 涓嬭惤鍓嶆偓鍋?tick 鏁?|
| `flight_target_speed` | 0.5 | 0.1~2.0 | 鍞竴閫熷害閰嶇疆锛堥┍鍔?lift脳0.16, drift脳0.04, noW脳0.8锛?|

#### Wall Run 娈?

| 閰嶇疆椤?| 榛樿鍊?| 鑼冨洿 | 璇存槑 |
|--------|--------|------|------|
| `wall_adhesion` | 0.05 | 0.01~0.5 | 姣?tick 鍚搁檮鎷夊悜澧欓潰鍔涘害 |
| `wall_friction` | 0.98 | 0.9~1.0 | 澧欓潰閫熷害琛板噺绯绘暟 |
| `wall_slide_speed` | 0.005 | 0.0~0.1 | 娌垮闈笅婊戦€熷害 |

#### Jump Charge 娈?

| 閰嶇疆椤?| 榛樿鍊?| 鑼冨洿 | 璇存槑 |
|--------|--------|------|------|
| `jump_threshold_ticks` | 20 | 5~100 | 钃勫姏闃堝€?|
| `jump_small_height` | 3.0 | 1.0~10.0 | 灏忚烦楂樺害 |
| `jump_charge_initial_bonus` | 5.0 | 1.0~30.0 | 钃勫姏鍒濆棰濆楂樺害 |
| `jump_charge_max_height` | 30.0 | 5.0~100.0 | 钃勫姏鏈€澶ч珮搴?|
| `jump_charge_max_ticks` | 60 | 20~200 | 鏈€闀胯搫鍔涙椂闂?|

### 16. 鍏抽敭璁捐鍐崇瓥

| 鍐崇瓥 | 鍘熷洜 |
|------|------|
| 鐘舵€佸瓨 PersistentData 鑰岄潪鐗╁搧 NBT | 鐜╁鍒囨崲 Curios 妲戒綅鍚?ItemStack 鏀瑰彉锛屼絾鐘舵€佸簲璺熺帺瀹惰蛋 |
| 鐜捣濮嬪崐寰?5 鏍?| 閬垮厤鍐插嚮涓績姝ｄ笅鏂规柟鍧楄鎸栫┖锛屼繚鐣欑珯鑴氫綅缃?|
| `ConcurrentHashMap` + `synchronized` | ACTIVE_SLAMS/PENDING_DAMAGES 鍙兘琚绾跨▼璁块棶 |
| `indirectMagic` 浜屾浼ゅ | 鏃犺鎶ょ敳鍜岄檮榄旓紝涓庡啿鍑荤殑 `playerAttack` 褰㈡垚浜掕ˉ |
| `findGroundPos` 鎵惧湴闈㈡柟鍧?| 鍏佽鐜姩鐢诲湪鎮┖娲炵┐涓壘鍒颁笅鏂瑰彲鐢ㄦ柟鍧?|
| 浜屾浼ゅ鍙栨秷鏂瑰潡涓婂崌鍜岀孩鍦?| 鍑忓皯瑙嗚鏉備贡锛屽彧淇濈暀鐧借壊鍦嗙幆 |
| 鐖涓ゅ眰 AABB 閲囨牱 | 鑴氶儴鍜屽ご椤跺悇閲囦竴鐐癸紝閬垮厤鍏?AABB 璇垽 |
| `try-catch` 鍖呰９ Curios 璋冪敤 | 闃叉 Curios 鏈姞杞芥椂 NPE 瀵艰嚧宕╂簝 |
| `flight_target_speed` 鍗曢厤缃┍鍔ㄥ叏閮ㄩ€熺巼 | 鏈潵淇敼鑰呭彧闇€鏀逛竴涓€硷紝lift/drift/noW 鎸夊唴閮ㄦ瘮鐜囪嚜鍔ㄧ缉鏀?|
| 淇啿浣跨敤 dot+gravity 鎶曞奖鑰岄潪 velocity-set | 淇濇寔閫熷害娌胯绾挎柟鍚戯紝閬垮厤淇啿鏃舵按骞?鍨傜洿涓嶅悓姝?|
| 涓嶆寜 W = 鎸?W 鐨?80% | 椋炶鏃舵湁鍖哄垎鎰燂紝浣嗘紓绉讳笉寮卞埌闅句互鎺у埗 |

## 璋冭瘯鏃ュ織

鎵€鏈夊叧閿簨浠惰緭鍑鸿嚦 `GoetyMechMod.LOGGER`锛圛NFO 绾у埆锛夛紝閫氳繃 `run/logs/latest.log` 鍙拷韪細
- 鐚涘嚮瑙﹀彂/鍙栨秷銆佸啿鍑讳激瀹?鍗婂緞/鐜暟
- AoE 鍛戒腑/璺宠繃瀹炰綋鍙婅窛绂?
- 鐜姩鐢荤殑姣忎釜鏂瑰潡浣嶇疆鍜?popUp 鍊?
- 鐖寮€濮?鍋滄銆佸闈㈡硶绾?
- 璺宠穬钃勫姏/閲婃斁/楂樺害
- Mixed 鍙栨秷鏅€氳烦璺冿紙`jumpChargeTick > 0`锛?

`PlayerTickEvent.START` 杩樿緭鍑?`zza` / `onGround` / `isFallFlying`锛圖EBUG 绾у埆锛夛紝鐢ㄤ簬璋冭瘯椋炶鐘舵€佹娴嬨€?
