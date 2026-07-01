---
name: "prototype-charge-jump"
description: "charge jump / jump height"
---

# Prototype 钃勫姏璺宠穬 鈥?寮€鍙?& 璋冨弬鎸囧崡

> 鍓嶇疆妯＄粍婧愮爜璺緞瑙?`.opencode/skills/_shared/libcode-paths.md`銆?> 蹇€熸帓闄わ細褰撳墠浠诲姟濡傛灉涓嶆秹鍙婅搫鍔?閲婃斁璺宠穬銆佽烦璺冮珮搴﹀叕寮忚皟鍙傛垨钀藉湴妫€娴嬶紝璇疯烦杩囨湰 skill銆?
## 閫傜敤鍦烘櫙

- 瀹炵幇钃勫姏-閲婃斁寮忥紙charge/release锛夎烦璺冩満鍒?- 鍏紡璁＄畻鐨勮烦璺冮珮搴︿笌瀹為檯娓告垙鍐呴珮搴︿笉绗?- 闇€瑕佺簿纭祴閲忓疄浣撹烦璺冮珮搴︼紙钀藉湴妫€娴嬶級
- 涓鸿烦璺冪郴缁熸坊鍔?Config 鍙皟鍙傛暟
- 鐞嗚В Minecraft 鍨傜洿杩愬姩鐗╃悊锛坓ravity / drag 鏃跺簭锛?- 璋冭瘯 Curios 妲戒綅妫€娴?+ Mixin 璺宠穬鍘嬪埗

## 鏂囨。绱㈠紩

| 鏂囦欢 | 浼樺厛绾?| 鍐呭 |
|------|--------|------|
| [DEBUGGING.md](DEBUGGING.md) | 鈽?鏈€楂?| 澶氭 build-run 璋冭瘯缁忛獙銆乸eakY 杩借釜娉曘€乄AS_AIRBORNE 鐘舵€佹満銆佸疄娴嬫暟鎹紙50鈫?0.7, 80鈫?0锛?|
| [PHYSICS.md](PHYSICS.md) | 鈽?楂?| 鍚?drag 绮剧‘閫熷害鍏紡鎺ㄥ銆乻qrt(2gh) 澶辨晥鍘熷洜銆乿elocityForHeight 浜屽垎娉曞疄鐜?|
| [ARCHITECTURE.md](ARCHITECTURE.md) | 涓?| 绯荤粺鏋舵瀯锛歳elease-based 璁捐銆丯BT 钃勫姏 tick銆佺綉缁滃寘娴併€丼TART phase 鏃跺簭銆佸湴闈㈣捣椋炶鐩栭櫡闃?|

## 鍏抽敭鍙戠幇閫熸煡

| 闂 | 缁撹 |
|------|------|
| `sqrt(2 * g * h)` 涓轰粈涔堜笉鍑?| Minecraft 鍨傜洿鏈?0.98/tick drag锛屽叕寮忓亣璁剧湡绌?|
| 钀藉湴楂樺害涓轰粈涔堟祴寰?0 | `onGround()` 鍦?START phase 鏄笂 tick 鐘舵€侊紝涓斿钩闈㈣捣钀?Y 鐩稿悓 |
| 姝ｇ‘鐨勯珮搴︽祴閲忔柟寮?| 杩借釜椋炶涓渶澶?Y锛坧eakY锛夛紝涓嶆槸璧疯惤 Y 宸?|
| 80 鐞嗚鍊间负浣曞彧璺?~60 | 褰撳墠浜屽垎娉曞叕寮忓湪楂橀€熸浠嶆湁鍋忓樊锛堝緟璋冿級 |
| 钃勫姏璺虫病鐢熸晥 | 妫€鏌ュ湴闈㈣捣椋炰唬鐮?(`zza>0 && charge==0` 浼氳鐩栦负 2.0锛?|
| Curios 鍘熷瀷妫€娴嬩笉鍒?| 蹇呴』鍚屾椂鏌?chest + Curios 妲斤紙`hasAnyPrototype`锛?|
| 鏀归厤缃病鐢熸晥 | 闇€瑕佸垹闄?`run/config/GoetyMech-common.toml` 寮哄埗鍐嶇敓 |
