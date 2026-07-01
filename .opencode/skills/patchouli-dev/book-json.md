# book.json

`/data/<modid>/patchouli_books/<book_name>/book.json`

## 瀛楁琛?

| 瀛楁 | 绫诲瀷 | 蹇呴渶 | 璇存槑 |
|---|---|---|---|
| `name` | String | 鉁?| 鎵嬪唽鏄剧ず鍚嶇О锛屽彲鐢?i18n key |
| `landing_text` | String | 鉁?| 棣栭〉鏂囨湰锛屽彲鐢?i18n key锛屾敮鎸佹牸寮忓寲 |
| `use_resource_pack` | Boolean | - | 1.20 蹇呴』涓?`true` |
| `book_texture` | String | - | 鑳屾櫙绾圭悊锛岄璁?7 鑹?|
| `filler_texture` | String | - | 绌虹櫧椤靛～鍏呯汗鐞?|
| `crafting_texture` | String | - | 鍚堟垚椤甸潰鑳屾櫙绾圭悊 |
| `model` | String | - | 鐗╁搧妯″瀷锛岄璁?7 鑹?|
| `text_color` | String | - | 姝ｆ枃棰滆壊 hex "RRGGBB"锛岄粯璁?"000000" |
| `header_color` | String | - | 鏍囬棰滆壊锛岄粯璁?"333333" |
| `nameplate_color` | String | - | 棣栭〉涔﹀悕棰滆壊锛岄粯璁?"FFDD00" |
| `link_color` | String | - | 閾炬帴棰滆壊锛岄粯璁?"0000EE" |
| `link_hover_color` | String | - | 鎮仠閾炬帴棰滆壊锛岄粯璁?"8800EE" |
| `progress_bar_color` | String | - | 杩涘害鏉￠鑹诧紝榛樿 "FFFF55" |
| `progress_bar_background` | String | - | 杩涘害鏉¤儗鏅壊锛岄粯璁?"DDDDDD" |
| `open_sound` | String | - | 鎵撳紑涔﹂煶鏁?resource location |
| `flip_sound` | String | - | 缈婚〉闊虫晥 resource location |
| `index_icon` | String | - | 绱㈠紩鍥炬爣 |
| `pamphlet` | Boolean | - | 灏忓唽瀛愭ā寮?|
| `show_progress` | Boolean | - | 鏄剧ず杩涘害鏉★紝榛樿 true |
| `version` | String | - | 鐗堟湰鍙凤紝鏄剧ず "X Edition" |
| `subtitle` | String | - | 鍓爣棰?|
| `creative_tab` | String | - | 鍒涢€犳ā寮忔爣绛鹃〉 |
| `advancements_tab` | String | - | 鍏宠仈鐨勮繘搴︽爣绛鹃〉 ID |
| `dont_generate_book` | Boolean | - | 涓嶈嚜鍔ㄧ敓鎴愮墿鍝?|
| `custom_book_item` | String | - | 鑷畾涔変功鐗╁搧 |
| `show_toasts` | Boolean | - | 鏂拌瘝鏉?toast锛岄粯璁?true |
| `use_blocky_font` | Boolean | - | 鍘熺増鍍忕礌瀛椾綋锛岄粯璁?false |
| `i18n` | Boolean | - | 鍚敤 i18n lang key 鏌ユ壘锛岄粯璁?false |
| `macros` | Object | - | 鑷畾涔夋牸寮忓寲瀹?|
| `pause_game` | Boolean | - | 鎵撳紑鏃舵殏鍋滄父鎴忥紝榛樿 false |
| `text_overflow_mode` | Enum | - | overflow/resize/truncate |

## 鏈」鐩ず渚?

```json
{
  "name": "GoetyMech.guide_book.name",
  "landing_text": "GoetyMech.guide_book.landing",
  "subtitle": "GoetyMech.guide_book.subtitle",
  "version": 1,
  "book_texture": "patchouli:textures/gui/book_purple.png",
  "model": "patchouli:book_purple",
  "creative_tab": "tools_and_utilities",
  "use_resource_pack": true,
  "i18n": true,
  "show_progress": true,
  "macros": {
    "$(goety)": "$(#b0b)",
    "$(punk)": "$(#490)"
  }
}
```

## Lang Key

```properties
GoetyMech.guide_book.name=GoetyMech 鎸囧崡
GoetyMech.guide_book.landing=$(bold)Welcome!$(br2)娆㈣繋鏌ラ槄 GoetyMech 妯＄粍鐨勬墜鍐屻€?
GoetyMech.guide_book.subtitle=鏆楅粦宸ヨ壓鎸囧崡
```

---

# 鏂囨湰鏍煎紡鍖?

`$(...)` 鏍煎紡鐮佺郴缁熴€?

## 鏍煎紡鍖栫爜

| 鐮?| 璇存槑 |
|---|---|
| `$()` | 娓呴櫎鎵€鏈夋牸寮?|
| `$(br)` | 鎹㈣ |
| `$(br2)` / `$(p)` | 娈佃惤 |
| `$(li)` | 鍒楄〃椤癸紙鍙彔鍔犳暟瀛?`$(li2)`锛?|
| `$(#rrggbb)` | 鍗佸叚杩涘埗棰滆壊 |
| `$(#rgb)` | 缂╁啓鑹?|
| `$(0)`-`$(f)` | MC 棰滆壊鐮?|
| `$(k)`/`$(l)`/`$(m)`/`$(n)`/`$(o)` | 娣锋穯/鍔犵矖/鍒犻櫎绾?涓嬪垝绾?鏂滀綋 |
| `$(l:ID)` | 鍐呴儴閾炬帴 |
| `$(l:entryid#anchor)` | 閿氱偣閾炬帴 |
| `$(l:https://...)` | 澶栭儴閾炬帴 |
| `$(/l)` | 缁撴潫閾炬帴 |
| `$(t:tooltip)` | 鎮仠鎻愮ず |
| `$(/t)` | 缁撴潫 tooltip |
| `$(c:/command)` | 鐐瑰嚮鎵ц鍛戒护 |
| `$(/c)` | 缁撴潫鍛戒护 |
| `$(playername)` | 鐜╁鍚?|
| `$(k:keyname)` | 鎸夐敭鍚?|

## 榛樿瀹?

| 瀹?| 灞曞紑 |
|---|---|
| `$(obf)` | `$(k)` |
| `$(bold)` | `$(l)` |
| `$(strike)` | `$(m)` |
| `$(italic)` / `$(italics)` | `$(o)` |
| `$(list` | `$(li` |
| `$(reset)` / `$(clear)` | `$()` |
| `$(2br)` | `$(br2)` |
| `$(nocolor)` | `$(0)` |
| `$(item)` | `$(#b0b)` |
| `$(thing)` | `$(#490)` |
| `<br>` | `$(br)` |

## 鑷畾涔夊畯

鍦?`book.json` 鐨?`macros` 涓畾涔夛細

```json
{
  "macros": {
    "$(potato)": "$(#F00)POTATO SALAD$()",
    "<b>": "$(l)",
    "<i>": "$(o)",
    "</>": "$()"
  }
}
```

---

# ItemStack String 鏍煎紡

## 鍐欐硶

| 鍐欐硶 | 绀轰緥 |
|---|---|
| `namespace:path` | `"minecraft:stone"` |
| `namespace:path#count` | `"minecraft:stone#64"` |
| `namespace:path{nbt}` | `"minecraft:diamond_pickaxe{...}"` |
| `namespace:path#count{nbt}` | 缁勫悎 |
| `tag:namespace:tag_id` | `"tag:minecraft:axes"` |

澶氱墿鍝佺敤閫楀彿鍒嗛殧锛歚"minecraft:sword,tag:minecraft:axes,minecraft:stone#64"`

NBT 涓紩鍙峰彲鐢ㄥ崟寮曞彿 `'` 鎴栬浆涔?`\"`銆?
