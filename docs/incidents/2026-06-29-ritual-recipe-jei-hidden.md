# Goety 浠紡閰嶆柟 JEI 涓嶆樉绀?鈥?璋冭瘯璁板綍

status: verified

## 鐜拌薄

鎵€鏈?3 涓?cycle 閰嶆柟锛堣瘏鍜掓敞鍏ュ櫒锛夊拰 Tier I 浠紡閰嶆柟鍦?JEI 鏄剧ず姝ｅ父锛屼絾 Tier II/III 浠紡閰嶆柟涓嶅嚭鐜般€傛父鎴忔棩蹇楁棤鎶ラ敊锛堟棤 ParseException锛夈€?

## 鏍瑰洜

涓や釜鐙珛闂锛?*閰嶆柟 item ID 閿欒** + **鐗╁搧缂虹汗鐞?*銆?

### 闂 1锛? 涓?Goety 鐗╁搧 ID 涓庢簮鐮佷笉鍖归厤

3 涓?upgrade 閰嶆柟 JSON 涓互涓?ID 鍐欓敊锛?

| 閿欒 ID | 姝ｇ‘ ID | 鏉ユ簮锛圡odItems锛?|
|---------|---------|-----------------|
| `pale_metal_ingot` | `pale_steel_ingot` | line 64 |
| `dark_metal_ingot` | `dark_ingot` | line 65 |
| `night_core` | `heart_of_the_night` | line 94 |
| `unholy_cloth` | `unholy_fabric` | line 74 |
| `large_focus_bag` | `focus_pack` | line 202 |
| `pure_blood` | `unholy_blood` | line 124 |

杩欎簺閿欒 ID 瀵瑰簲鐨勭墿鍝佷笉瀛樺湪锛圙oety 浠庢湭鐢ㄨ繖浜涘悕绉版敞鍐岃繃锛夈€侴oety 鐨勯厤鏂硅В鏋愬櫒涓嶆姤閿欙紝浣嗚烦杩囪繖浜涢厤鏂?鈫?JEI 涓嶆灇涓俱€?

**鏁欒**锛欸oety 鐨?RecipeType 娌℃湁 `ingredients` 涓ユ牸鏍￠獙锛屾棤鏁?ingredient item ID 闈欓粯澶辫触銆傚繀椤诲鐓?Goety `ModItems.java` 婧愮爜閫愰」纭銆?

### 闂 2锛氱墿鍝佺己绾圭悊

Tier I 浣跨敤 `goety:craft` 浠紡绫诲瀷濮嬬粓鍙敤锛屼絾 `flesh_casket_1/2/3` 娌℃湁瀵瑰簲 `textures/item/` 涓嬬殑 PNG锛屽鑷寸墿鍝佹爮鏄剧ず绱粦鍧椼€傝繖瀵逛簬 JEI 鏄剧ず铏界劧鏈夊奖鍝嶏紝浣嗕笉鏄牴鏈師鍥狅紙Tier I 鍦ㄧ己绾圭悊鏃朵粛鏄剧ず锛夈€?

**鏁欒**锛氭敞鍐屾柊鐗╁搧鍚庣珛鍗冲垱寤哄崰浣嶇汗鐞嗭紝鍚﹀垯瑙嗚寮傚父浼氬共鎵伴棶棰樺畾浣嶃€?

## 淇

1. 淇 3 涓?JSON 涓殑 6 涓?item ID
2. 鍒涘缓 `assets/GoetyMech/textures/item/flesh_casket_{1,2,3}.png` 鍗犱綅绾圭悊锛?脳3px 鑹插潡锛屽鍒惰嚜 prototype.png锛?
3. 閲嶆柊鎵撳寘鍚姩鍚?3 涓崌绾ч厤鏂瑰叏閮ㄦ樉绀?

## 楠岃瘉

- `runClient` 鍚姩鍒颁笘鐣?
- JEI 鏌ョ湅鎵€鏈?6 涓厤鏂癸紙3 cycle + 3 ritual锛夊潎鍙
- 鏃ュ織涓棤 `flesh_casket` 鐩稿叧寮傚父

## 鐩稿叧鏂囦欢涓庤矾寰?

- `src/main/resources/data/GoetyMech/recipes/flesh_casket_1_to_2.json`
- `src/main/resources/data/GoetyMech/recipes/flesh_casket_2_to_3.json`
- `src/main/resources/assets/GoetyMech/textures/item/flesh_casket_{1,2,3}.png`
- `D:\Libcode\goety\src\main\java\com\Polarice3\Goety\init\ModItems.java`锛堥獙璇佹簮锛?
