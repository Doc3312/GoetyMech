# 鍘熷瀷 鈥?GoetyMech

## 韬唤

- 绫诲瀷锛歟lytra
- 娉ㄥ唽绫伙細ModItems.PROTOTYPE
- Java 绫伙細PrototypeItem (extends ElytraItem)
- 棣栨鍒涘缓锛?026-06-14

## 璁捐鎰忓浘

澶氬姛鑳芥垬鏂楅灅缈呫€傛彁渚涳細鑷畾涔夐琛岀墿鐞嗭紙velocity-set 妯″瀷锛夈€佸璺戠郴缁熴€佽搫鍔涜烦璺冦€丼lam 鍐插嚮鎶€鑳姐€傛牳蹇冪帺娉曟槸"椋炶鏈哄姩 + 杩戞垬鍘嬫潃"銆?

## 婕斿寲璁板綍

### 2026-06-23 鈥?澧欒窇鍏ㄩ噺鏃嬭浆瀹炵幇 + 鐗╃悊澶辫触褰掓。

- 鏀瑰姩锛氬疄鐜扮浉鏈哄叏閲忔棆杞紙鍥涘厓鏁?SLERP 骞虫粦杩囨浮锛夈€佽浆瑙掕嚜鍔ㄦ娴嬶紙findBestWallDirection锛夈€佸闈㈡懇鎿︽娴嬶紙slipperiness 鈮?0.8 绂佹璺戝锛夈€佽窇澧欐縺娲绘潯浠舵斁瀹斤紙鍦伴潰鍐插埡涔熷彲婵€娲伙級锛涚墿鐞嗗眰鏀圭敤 `player.move() + setDeltaMovement(ZERO)` 妯″瀷锛屼絾浠嶆棤娉曚骇鐢熷疄闄呬綅绉?
- 鍘熷洜锛氱浉鏈烘棆杞渶瑕佸畬鏁寸殑澧欏=鍦伴潰瑙嗚浣撻獙锛涚墿鐞嗗眰 `move()` 鎵ц浣?Y 鍧愭爣涓嶅彉鍖栵紝寮€鏀鹃棶棰樺緟鍚庣画璇婃柇
- 鍏宠仈锛歸all-run-full-rotation change锛堝凡褰掓。锛?

### 2026-06-18 鈥?澧欒窇鐗╃悊淇锛歒 杞寸嫭绔嬫帶鍒?

- 鏀瑰姩锛歛pplyWallMovement() 涓嶅啀缁ф壙鐜╁涓嬭惤閫熷害锛孻 杞磋涓虹嫭绔嬫參婊戦€熷害 WALL_SLIDE_SPEED锛涜绾挎柟鍚戜繚鐣?Y 鍒嗛噺鏀寔浠板ご鐖锛涢€€鍑烘潯浠朵粠涓嶅啿鍒烘敼涓鸿惤鍦?
- 鍘熷洜锛氬師浠ｇ爜缁ф壙鐜╁褰撳墠閫熷害鐨?Y 鍒嗛噺锛?0.08/tick锛夛紝涔樻懇鎿﹀悗浠嶄互閲嶅姏閫熷害鍧犺惤
- 鍏宠仈锛歸allrun-physics-fix issue

### 2026-06-18 鈥?鍒犻櫎澧欒窇璺崇銆侀琛屽姞閫熷姛鑳?

- 鏀惧純鏂规锛歨andleWallJumpOff + C2SWallJumpOffPacket銆丼TORED_ZZA 瀛楁椋炶鍔犻€熴€佸湴闈㈣捣椋炵┖涓己鍒堕琛?
- 鏀惧純鍘熷洜锛氬璺戣烦绂讳笌钃勫姏璺冲啿绐侊紙鍏变韩鎸夐敭缁戝畾锛夛紱椋炶鍔犻€熻 velocity-set 妯″瀷鏇夸唬
- 鍏宠仈锛歸allrun-physics-fix issue

### 2026-06-18 鈥?椋炶鐗╃悊浠庡姞娉曞姞閫熷櫒杩佺Щ鍒?velocity-set 妯″瀷

- 鏀瑰姩锛氶琛屽姞閫熶粠鍔犳硶鍙犲姞鏀逛负姣忔鐩存帴璁剧疆鍚堟垚鍚庣殑鏈€缁堥€熷害
- 鍘熷洜锛氬姞娉曞姞閫熷櫒鍦ㄥ鎴风-鏈嶅姟绔悓姝ヤ腑瀛樺湪 forward 鏂瑰悜涓嶄竴鑷撮棶棰?
- 鍏宠仈锛歱rototype-flight-mixin skill

### 2026-06-23 鈥?钃勫姏璺冲啿鍒烘儻鎬т慨澶?+ 閫熷害鍊嶇巼

- 鏀瑰姩锛氭柊澧?cj_start_speed NBT 淇濈暀钃勫姏寮€濮嬫椂瀹㈡埛绔€熷害锛汣J 娉ㄥ叆涓嶅啀琚?isFallFlying() 闃诲锛岄琛屼腑鎸佺画缁存寔姘村钩閫熷害锛涘湪 applyChargedJump 涓粰 bestSpeed 涔?2.5 鍊?
- 鍘熷洜锛氶暱钃勫姏锛?0+ tick锛夊悗 isSprinting() 涓㈠け瀵艰嚧 attrSpeed 濉岀缉鍒?0.1锛涜繘鍏ラ琛屽悗 CJ 娉ㄥ叆琚烦杩囷紝姘村钩閫熷害浠呴潬 elytra 鐗╃悊缁存寔锛堟懇鎿﹀ぇ锛夛紱鐢ㄦ埛鍙嶉閫熷害浠嶉渶鎻愬崌
- 鍏宠仈锛歠ix-charge-jump-hspeed change

### 2026-06-23 鈥?淇 sprint 鍋滄鍚?groundSpeed 娈嬪€煎鑷存棤杈撳叆钃勫姏璺宠幏寰楅敊璇按骞冲姩閲?

- 鏀瑰姩锛氬湪 sprint 鍋滄鍒嗘敮锛坄!player.isSprinting()`锛変腑娓呯┖ `groundSpeed(NBT)`
- 鍘熷洜锛歴print 鏈熼棿姣?tick 杩借釜鐨勫疄娴嬮€熷害娈嬬暀鍦?NBT 涓紝鍋滄 sprint 鍚庢湭琚竻闄わ紝钃勫姏璺虫椂 `bestSpeed` 鍙栧埌鏃?sprint 閫熷害锛屽湪鏃?W 杈撳叆鏃朵骇鐢熶笉搴旀湁鐨勬按骞虫帹鍔?
- 鍏宠仈锛歝odebase-remediation change

### 2026-06-23 鈥?澧欒窇鏂瑰悜璁＄畻浠庤绾挎姇褰辨敼涓烘按骞虫部澧欐柟鍚?

- 鏀瑰姩锛歴print 鏃剁敤 `down 脳 wallN` 璁＄畻姘村钩娌垮鏂瑰悜锛屾浛浠?`getLookAngle()` 鎶曞奖锛涙柊澧?C2SWallInputPacket锛堝鎴风 鈫?鏈嶅姟绔悓姝?forward/strafe 杈撳叆锛夛紱澧欒窇鏃?`forward=1` fallback
- 鍘熷洜锛氳绾挎姇褰卞湪澧欓潰涓婁骇鐢熶笉鍙娴嬬殑鏂滃悜绉诲姩锛屼笉婊¤冻"姘村钩娌垮璺?鐨勯鏈?
- 鍏宠仈锛歸all-run iteration (commits c544224 鈫?565daa0 鈫?136150e 鈫?5faa6a5)

### 2026-06-23 鈥?澧欒窇鐗╃悊鏀逛负鐩存帴 move() 缁曡繃 travel() 鎽╂摝

- 鏀瑰姩锛氬彇娑?setDeltaMovement 琚姩鐗╃悊锛屾敼涓?`player.move(MoverType.SELF, delta)` 鐩存帴椹卞姩锛沝elta 娓呴浂闃叉 travel() 瑕嗙洊锛涚Щ闄?sprint 妫€鏌ヤ娇澧欒窇鍦?sprint 鍙栨秷鍚庢寔缁?
- 鍘熷洜锛歵ravel() 鐨勬懇鎿﹁鐩?+ onGround 妫€娴嬪鑷村璺戦€熷害琚ぇ骞呰“鍑忥紝闇€瑕佺粫杩囧師鐗堢Щ鍔ㄦ祦姘寸嚎
- 鍏宠仈锛歸all-run iteration (commits 1f5853d 鈫?c755f7e 鈫?cd816fd)

### 2026-06-23 鈥?澧欒窇鍚姩鏃舵姮鍗囩鍦?+ onGround 瀹堝崼

- 鏀瑰姩锛氬璺戞縺娲绘椂寮哄埗鎶崌 0.2 鏍硷紙閬垮厤鍦伴潰鎽╂摝鎶垫秷姘村钩閫熷害锛夛紱鍦?applyWallMovement 鍏ュ彛鍔?`player.onGround()` 瀹堝崼
- 鍘熷洜锛氬璺戜粠鍦伴潰寮€濮嬫椂锛宼ravel() 鐨勫湴闈㈡懇鎿︾珛鍗冲皢閫熷害闄嶅埌闆讹紝闇€瑕佺┖涓惎鍔?
- 鍏宠仈锛歝ommit 9ac4f32

### 2026-06-23 鈥?澧欒窇鏀圭敤瑙嗙嚎鏂瑰悜 + 鍙嶉噸鍔涙ā寮?

- 鏀瑰姩锛氬璺戞柟鍚戞敼鍥炶绾挎柟鍚戯紙`getLookAngle()`锛夛紝绉婚櫎 lift-off-ground锛涙敼涓烘寔缁缃?`setNoGravity(true)` 娑堥櫎閲嶅姏褰卞搷
- 鍘熷洜锛歭ift-off-ground 鍦ㄥ鏉傚湴褰笉鍙潬锛涘弽閲嶅姏姣旀姮鍗囨洿绋冲畾
- 鍏宠仈锛歝ommit 44f9fb3

### 2026-06-23 鈥?鍨傜洿澧欒窇瀹炵幇锛歐 鐖崌 / S 涓嬫粦 / A D 姘村钩妯Щ

- 鏀瑰姩锛氬璺戞柟鍚戜粠姘村钩娌垮锛坄forwardOnWall`锛夋敼涓哄瀭鐩村悜涓婏紙`wallUp`锛夛細W=鐖崌銆丼=涓嬫粦銆丄/D=宸﹀彸妯Щ锛涢噸鍔涗粠澧欓潰鎶曞奖鏀逛负鎷夊悜澧欓潰锛坄wallN * -WALL_GRAVITY`锛夛紱鏂板 `WALL_PULL_SPEED` 淇濇寔璐村
- 鍘熷洜锛氭按骞虫部澧欒窇涓嶇鍚?Titanfall 寮忓瀭鐩村璺戠殑棰勬湡锛涢渶瑕?W/S 鎺у埗鍗囬檷銆丄/D 鎺у埗姘村钩妯Щ
- 鍏宠仈锛歝ommit 99b7245锛圥rototypeRenderEvents.java client-side setDeltaMovement锛夛紱Config keys: `WALL_RUN_SPEED`, `WALL_GRAVITY`, `WALL_PULL_SPEED`

### 2026-06-23 鈥?Plan B: Mixin into Player.travel() for wall-run movement

- 鏀瑰姩锛欻EAD injection 鍙栨秷 travel()锛岄€氳繃 accessor 璁剧疆 onGround=true锛屾墜鍔ㄨ皟鐢?player.move() 浼犲叆澧欒窇閫熷害锛圵=wallUp, A/D=wallRight锛岄噸鍔涙媺鍚戝闈?+ 璐村鎷夊姏锛夛紱閫氳繃 ClientWallRunState 閲嶆柊搴旂敤 sprint锛涗复鏃?onGround 瑕嗙洊瀹炵幇 step-up
- 鍘熷洜锛歵ravel() 鐨勬懇鎿︽ā鍨?+ onGround 妫€娴嬪鑷村彇娑?sprint 鍚庡璺戦€熷害琚ぇ骞呰“鍑忥紱闇€瑕佸畬鍏ㄧ粫杩囧師鐗堢Щ鍔ㄦ祦姘寸嚎浠ヨ揪鍒?Titanfall 寮忓瀭鐩村璺戞晥鏋?
- 鍏宠仈锛歝ommit 7a632bf锛涙枃浠讹細MixinWallRunTravel.java銆丆lientWallRunState.java銆丒ntityOnGroundAccessor.java銆丳rototypeRenderEvents.java

### 2026-06-24 鈥?澧欒窇澶撮《缈昏秺锛坥utward burst + grace period锛? 缈昏秺鍚?sprint 淇濇寔

- 鏀瑰姩锛氬璺戦亣鍒板ご椤堕殰纰嶆椂鑷姩鍚戝鐖嗗彂锛坥utward burst 0.9/1.2/0.7/0.5锛夊皾璇曠炕瓒婏紝鎴愬姛璁?3 tick grace 绐楀彛鐢?pull 閲嶆柊璐村锛涚炕瓒婂悗 sprintRetainTicks=30锛?.5绉掞級淇濇寔鍐插埡鐘舵€侊紱Shift 閿€€鍑哄璺?
- 鍘熷洜锛氬師鐗?travel() 閬囧ぉ鑺辨澘纰版挒鏃犳硶缁х画鐖锛汱ocalPlayer.aiStep() 鍦?travel() 鍚庢竻闄?sprint锛岀炕瓒婂悗 sprint 涓㈠け
- 鍏宠仈锛歁ixinWallRunTravel.java, ClientWallRunState.java, PrototypeRenderEvents.java; sprintRetain 鐢辩敤鎴锋墜鍔ㄤ粠 10 鏀逛负 30 tick

### 2026-06-24 鈥?Sprint cut-off 淇 + 姘翠笅澧欒窇瀹堝崼 + 娓告吵鍔犻€熼噸鍐?

- 鏀瑰姩锛氱Щ闄?shouldSprint() 瀹堝崼锛堟仮澶嶇洿鎺ユ寜閿疆璇級锛涙按涓嬬姝㈠璺戯紙绉诲姩+鐩告満锛夛紱娓告吵鍔犻€熶粠 attribute modifier 鏀逛负 mixin 鍏紡 `m = 1.25 - 0.25/d`锛屼笁杞翠箻娉曚繚鐣欎刊浠拌
- 鍘熷洜锛歴print 鍦ㄧ炕瓒婂悗鏂帀锛坰houldSprint guard 闃绘 sprintDown 鍒锋柊锛夛紱姘翠笅澧欒窇涓嶅簲瑙﹀彂锛汳OVEMENT_SPEED 灞炴€у湪娓告吵鏃舵棤 Depths Strider 鏃朵笉鐢熸晥锛宎ttribute 鏂规硶涓嶅彲琛?
- 鍏宠仈锛歴print-cutoff-fix, underwater-wallrun-guard, swim-accel-rework

### 2026-06-25 鈥?鍚屽寲鍑绘潃璁℃暟鎴愰暱绯荤粺锛堟粦缈旀敼涓哄垵濮嬭兘鍔?+ 鏂囦欢缂撳瓨闃插穿婧冧涪澶憋級

- 鏀瑰姩锛氭粦缈旀敼涓哄缁堝彲鐢紙hasFlight 杩斿洖 true锛夛紱鏂板 AssimilcationSaveManager 姣?60s 鍐欑嫭绔嬬紦瀛樻枃浠跺埌 playerdata/锛屽啓鏃跺厛鍐?.tmp 鍐嶅師瀛愰噸鍛藉悕锛涚櫥褰曟椂 cache > NBT 鑷姩鎭㈠鍑绘潃鏁?
- 鍘熷洜锛氱敤鎴峰彂鐜伴灅缈呮棤娉曠鐢ㄦ粦缈旓紱缁忓巻 crash 瀵艰嚧鍑绘潃鏁版竻闆剁殑鏁欒锛岄渶瑕佺嫭绔嬩簬 NBT 鐨勫浠芥満鍒?

### 2026-06-25 鈥?鍚屽寲鍑绘潃璁℃暟鎴愰暱绯荤粺

- 鏀瑰姩锛氭柊澧炲悓鍖栧嚮鏉€璁℃暟绯荤粺锛圥ersistentData `GoetyMech.assimilation_kills`锛夛紝4 闃惰兘鍔涜В閿侊紙<10=鍩虹鏈哄姩+婊戠繑 鈮?0=浜岀骇鍔犻€?鈮?0=Slam 鈮?00=缁堥樁锛夛紝杈归檯鏀剁泭浼ゅ鍊嶇巼鍑芥暟锛?-100k+ 鏃犳瀬 cap锛夛紝鐥呮瘨闄勫姞浼ゅ锛坕ndirectMagic per hit锛夛紝Shift 灞曞紑 tooltip 鏄剧ず鑳藉姏鍒楄〃/鐘舵€?缁熻
- 鍘熷洜锛氫负 Prototype 鎻愪緵鎴愰暱鐨?RPG 瑕佺礌锛屽嚮鏉€瓒婂鑳藉姏瓒婂己
- 鍏宠仈锛歋2CAssimilationDataPacket.java, PrototypeItem.java (tier/damage utils), PrototypeEvents.java (kill tracking, login sync, damage mult), PrototypeTravelMixin.java (椋炶/娓告吵 tier scaling), MixinWallRunTravel.java (澧欒窇 tier scaling), PrototypeRenderEvents.java (tooltip), zh_cn.json / en_us.json

### 2026-06-25 鈥?Mimic 鍚堟垚浣撶郴 + 涓嶅彲鎽ф瘉瀹堝崼 + 缁熶竴妫€娴?

- 鏀瑰姩锛氭柊澧?`isPrototype(ItemStack)` / `isPrototypeItem(Item)` / `hasAnyPrototype()` 缁熶竴妫€娴嬫柟娉曪紱ItemEntityPrototypeMixin 鍏嶇柅浼ゅ锛汭temEntityVoidMixin 铏氱┖鎷夊洖 Y=260锛汸rototypeMimicItem `createFrom()` 鍚堟垚缁ф壙灞炴€э紱MimicCombinationRecipe + MimicDeconstructionRecipe 娉ㄥ唽
- 鍘熷洜锛歱rototype 鐗╁搧闇€瑕佺粺涓€鐨勫綊灞炴娴嬫帴鍙ｏ紱mimic 鍚堟垚绯荤粺鍏佽灏?prototype+ 鑳哥敳鍚堟垚涓哄彲绌挎埓 ElytraItem

### 2026-06-25 鈥?鐨笅鎶ょ敳锛氬嚮鏉€鎴愰暱鍨嬪厤浼ょ郴缁?

- 鏀瑰姩锛氭柊澧?`getDamageImmunityThreshold(int kills)` 涓夋鍏紡璁＄畻鍏嶄激闃堝€硷紱`getNextImmunityMilestone(int kills)` 璁＄畻涓嬫杩涘寲鎵€闇€鍑绘潃锛沗LivingHurtEvent` 涓彈瀹宠€呭厤浼ゅ垽瀹氾紙鏈夋潵婧愪激瀹充笖 `amount <= threshold` 鏃?cancel 浜嬩欢锛屾挱鏀?EXP_ORB 鍙０锛夛紱tooltip 瀛愬脊淇啿鍚庢柊澧?鐨笅鎶ょ敳"琛岋紙绾?娣辩伆锛夛紝鏀诲嚮鍔涘姞鎴愯鏈熬杩藉姞"浼ゅ鍏嶇柅锛歑"
- 鍏紡锛歚<150=0, 150~1000=kills/100, 1000~10000=10+(kills-1000)/1000, 10000+=19+(kills-10000)/2000`
- 鍘熷洜锛氫负 prototype 鎻愪緵涓庨澶栫敓鍛?鎶ょ浘涓嶅悓鐨勭涓夌被闃插尽鏈哄埗锛涗綆浼ゅ琚畬鍏ㄦ棤瑙嗭紝闅忓嚮鏉€澧為暱闃堝€兼彁楂?
- 鏁欒锛氬垵濮?`(kills-150)/200+1` 澧為€熷お鎱紙200 鏉€鎵?1锛夛紝鐢ㄦ埛璁や负鍚噾閲忎笉瓒筹紝鏀逛负 `kills/100` 鍚庢瘡 100 鍑绘潃绋冲畾+1
- 鍏宠仈锛歅rototypeItem.java (formula), PrototypeEvents.java (immunity check), PrototypeRenderEvents.java (tooltip)

### 2026-06-23 鈥?浣撶嚎绮掑瓙绯荤粺閲嶆瀯锛? 閿氱偣 + 涓嬬牳 FLAME 鐏姳

- 鏀瑰姩锛氱Щ闄ゆ棫鍐插埡绮掑瓙锛圖ustParticleOptions 闅忔満鏁ｅ竷锛夊拰钃勫姏璺崇矑瀛愶紙CRIT + ENCHANTED_HIT锛夛紱鏂板 6 韬綋閿氱偣绮掑瓙绯荤粺锛堝乏鎵?鍙虫墜澶栦晶銆佸ご椤跺乏/鍙炽€佸乏鑴?鍙宠剼澶栦晶锛夛紱S2CSlamStatePacket 鍚屾涓嬬牳瀹㈡埛绔姸鎬侊紱涓嬬牳鏃剁敓鎴愭繁绾綋绾?+ FLAME 鐏槦 + 娣＄孩灏捐抗涓夊眰娣峰悎
- 鍘熷洜锛氱矑瀛愪綅缃洿缁嗚嚧锛岃韩浣撳悇閮ㄤ綅鍚勬湁灏捐抗锛涗笅鐮歌瑙夊尯鍒嗗害涓嶈冻
- 鍏宠仈锛歱article-rework change
