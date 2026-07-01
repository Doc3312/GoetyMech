---
name: "GoetyMech-architecture"
description: "GoetyMech 椤圭洰鐗规湁鏋舵瀯瑙勮寖銆傚畾涔夋湰椤圭洰鐨勫寘缁撴瀯銆佸垎灞傝竟鐣屻€佹敞鍐岀害瀹氬拰璁捐鍐崇瓥銆傚綋娣诲姞鏂板姛鑳姐€佹柊寤虹被銆佽皟鏁撮」鐩粨鏋勬椂璋冪敤鏈?skill銆傞€氱敤 Forge/Goety 寮€鍙戠煡璇嗗湪 goety-addon-dev 涓€?
---

# GoetyMech 椤圭洰鏋舵瀯瑙勮寖

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
鈹?  鈹?  鈹斺攢鈹€ MatureEmbryoItem.java
鈹?  鈹溾攢鈹€ block/
鈹?  鈹?  鈹溾攢鈹€ FleshFurnaceBlock.java
鈹?  鈹?  鈹斺攢鈹€ entity/FleshFurnaceBlockEntity.java
鈹?  鈹溾攢鈹€ entity/
鈹?  鈹?  鈹斺攢鈹€ SculkSpikeEntity.java
鈹?  鈹溾攢鈹€ magic/spell/
鈹?  鈹?  鈹斺攢鈹€ SculkBindSpell.java
鈹?  鈹溾攢鈹€ events/
鈹?  鈹?  鈹斺攢鈹€ SculkBindEvents.java
鈹?  鈹斺攢鈹€ utils/
鈹?      鈹斺攢鈹€ MinionSpawnHelper.java
鈹斺攢鈹€ client/                        # 瀹㈡埛绔笓鐢?    鈹溾攢鈹€ ModClientEvents.java
    鈹溾攢鈹€ FleshFurnaceHUD.java
    鈹斺攢鈹€ entity/SculkSpikeRenderer.java
```

---

## 涓夈€佸垎灞傜害瀹?
| 鍖?| 鑱岃矗 | 绂佹 |
|----|------|------|
| `init/` | DeferredRegister + RegistryObject | 涓氬姟閫昏緫銆佸尶鍚嶅唴閮ㄧ被 |
| `common/item/` | 鐗╁搧浜や簰閫昏緫锛坲se/useOn锛?| 娉ㄥ唽閫昏緫 |
| `common/block/` | 鏂瑰潡琛屼负 + entity/ 鏂瑰潡瀹炰綋 tick | 瀹㈡埛绔唬鐮?|
| `common/entity/` | 瀹炰綋 AI銆佸姩鐢荤姸鎬佹満 | 娓叉煋浠ｇ爜 |
| `common/magic/` | Spell 鈫?spelle/ 瀛愬寘 | 鐗╁搧娉ㄥ唽 |
| `common/events/` | `@Mod.EventBusSubscriber` | 娉ㄥ唽閫昏緫 |
| `common/utils/` | 绾潤鎬佸伐鍏锋柟娉?| 瀹炰緥鐘舵€?|
| `client/` | 娓叉煋/HUD/GUI | 鏈嶅姟绔€昏緫 |

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