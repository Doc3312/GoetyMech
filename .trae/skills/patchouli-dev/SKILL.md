---
name: "patchouli-dev"
description: "Patchouli 鎸囧崡涔﹀紑鍙戞寚鍗椼€傛兜鐩?book.json 閰嶇疆銆丆ategory/Entry JSON銆侀〉闈㈢被鍨嬨€佹ā鏉跨郴缁熴€佷唬鐮?API銆丆onfig Gating銆傚綋闇€瑕佸垱寤烘垨淇敼妯＄粍鎸囧崡涔︽椂璋冪敤鏈?skill銆?
---

# Patchouli 鎸囧崡涔﹀紑鍙戞寚鍗?
## book.json

`/data/<modid>/patchouli_books/<book_name>/book.json`

| 瀛楁 | 绫诲瀷 | 蹇呴渶 | 璇存槑 |
|---|---|---|---|
| `name` | String | 鉁?| 鎵嬪唽鏄剧ず鍚嶇О |
| `landing_text` | String | 鉁?| 棣栭〉鏂囨湰 |
| `use_resource_pack` | Boolean | - | 1.20 蹇呴』涓?`true` |
| `book_texture` | String | - | 鑳屾櫙绾圭悊锛岄璁?7 鑹?|
| `model` | String | - | 鐗╁搧妯″瀷锛岄璁?7 鑹?|
| `version` | String | - | 鐗堟湰鍙?|
| `subtitle` | String | - | 鍓爣棰?|
| `i18n` | Boolean | - | 鍚敤 i18n lang key 鏌ユ壘 |
| `pause_game` | Boolean | - | 鎵撳紑鏃舵殏鍋滄父鎴?|

### 鏈」鐩ず渚?```json
{
  "name": "GoetyMech.guide_book.name",
  "landing_text": "GoetyMech.guide_book.landing",
  "subtitle": "GoetyMech.guide_book.subtitle",
  "version": 1,
  "book_texture": "patchouli:textures/gui/book_purple.png",
  "model": "patchouli:book_purple",
  "use_resource_pack": true
}
```

---

## Category JSON

`/assets/<modid>/patchouli_books/<book_name>/en_us/categories/<name>.json`

| 瀛楁 | 绫诲瀷 | 蹇呴渶 | 璇存槑 |
|---|---|---|---|
| `name` | String | 鉁?| 鍒嗙被鍚嶇О |
| `description` | String | 鉁?| 鍒嗙被鎻忚堪 |
| `icon` | String | 鉁?| 鍥炬爣锛圛temStack String 鎴?.png 绾圭悊锛?|
| `parent` | String | - | 鐖跺垎绫?ID |
| `flag` | String | - | Config flag 琛ㄨ揪寮?|
| `sortnum` | Integer | - | 鎺掑簭鍙?|
| `secret` | Boolean | - | 闅愯棌鍒嗙被 |

```json
{
  "name": "GoetyMech.category.machines",
  "description": "GoetyMech 娣诲姞鐨勫悇绫绘満鍣?,
  "icon": "GoetyMech:example_machine",
  "sortnum": 0
}
```

---

## Entry JSON

`/assets/<modid>/patchouli_books/<book_name>/en_us/entries/<category>/<name>.json`

| 瀛楁 | 绫诲瀷 | 蹇呴渶 | 璇存槑 |
|---|---|---|---|
| `name` | String | 鉁?| 璇嶆潯鍚嶇О |
| `category` | String | 鉁?| 鎵€灞炲垎绫?|
| `icon` | String | 鉁?| 鍥炬爣 |
| `pages` | Array | 鉁?| 椤甸潰鏁扮粍 |
| `priority` | Boolean | - | 缃《 |
| `secret` | Boolean | - | 闅愯棌 |
| `read_by_default` | Boolean | - | 榛樿宸茶 |
| `sortnum` | Integer | - | 鎺掑簭鍙?|
| `advancement` | String | - | 閿佸畾杩涘害 |
| `flag` | String | - | Config flag |

---

## 榛樿 Page Types

鎵€鏈夐〉闈㈤€氱敤瀛楁锛歚type`(蹇呴渶)銆乣advancement`銆乣flag`銆乣anchor`銆?
### text `patchouli:text`
| 瀛楁 | 蹇呴渶 | 璇存槑 |
|---|---|---|
| `text` | 鉁?| 姝ｆ枃锛屾敮鎸佹牸寮忓寲 |
| `title` | - | 鏍囬 |

棣栭〉蹇呴』鏄?text 椤点€傚揩鎹峰啓娉曪細`"pages": ["杩欐槸涓€娈电函鏂囨湰"]`

### image `patchouli:image`
| 瀛楁 | 蹇呴渶 | 璇存槑 |
|---|---|---|
| `images` | 鉁?| String 鏁扮粍 |
| `title` | - | 鏍囬 |
| `border` | - | 杈规锛岄粯璁?false |
| `text` | - | 璇存槑鏂囧瓧 |

寤鸿鍥剧墖 256x256锛屽唴瀹瑰湪宸︿笂 200x200 鍖哄煙銆?
### crafting `patchouli:crafting`
| 瀛楁 | 蹇呴渶 | 璇存槑 |
|---|---|---|
| `recipe` | 鉁?| 閰嶆柟 ID |
| `recipe2` | - | 绗簩涓厤鏂?ID |
| `text` | - | 璇存槑 |

### smelting `patchouli:smelting`
| 瀛楁 | 蹇呴渶 | 璇存槑 |
|---|---|---|
| `recipe` | 鉁?| 閰嶆柟 ID |
| `recipe2` | - | 绗簩涓厤鏂?ID |

### multiblock `patchouli:multiblock`
| 瀛楁 | 蹇呴渶 | 璇存槑 |
|---|---|---|
| `multiblock` | 鉁?| 澶氭柟鍧?ID |
| `name` | 鉁?| 澶氭柟鍧楀悕绉?|
| `text` | - | 璇存槑 |
| `enable_visualize` | - | 鍙鍖栨寜閽?|

### spotlight `patchouli:spotlight`
| 瀛楁 | 蹇呴渶 | 璇存槑 |
|---|---|---|
| `item` | 鉁?| 鑱氱劍鐗╁搧 |
| `text` | - | 璇存槑 |
| `link_recipe` | - | 鐐瑰嚮璺宠浆閰嶆柟 |

### entity `patchouli:entity`
| 瀛楁 | 蹇呴渶 | 璇存槑 |
|---|---|---|
| `entity` | 鉁?| 瀹炰綋 ID |
| `text` | - | 璇存槑 |
| `scale` | - | 缂╂斁 |
| `offset` | - | 鍋忕Щ |

---

## Template 绯荤粺

### 妯℃澘 JSON
`/assets/<modid>/patchouli_books/<book_name>/en_us/templates/<name>.json`

```json
{
  "components": [
    { "type": "patchouli:text", "text": "#text", "x": 20, "y": 30 },
    { "type": "patchouli:item", "item": "#item", "x": 80, "y": 10, "link_recipe": true }
  ],
  "processor": "com.GoetyMech.addon.jei.MyProcessor"
}
```

鍓嶇紑 `#` 鐨勪负鍙橀噺锛屽湪 entry 涓祴鍊笺€?
### 缁勪欢绫诲瀷
| 绫诲瀷 | 鏍囪瘑 | 璇存槑 |
|---|---|---|
| 鏂囨湰 | `patchouli:text` | `text`(蹇呴渶), `color`, `max_width`, `line_height` |
| 鐗╁搧 | `patchouli:item` | `item`(蹇呴渶), `framed`, `link_recipe` |
| 鍥剧墖 | `patchouli:image` | `image`(蹇呴渶), `width`, `height`, `u`, `v`, `scale` |
| 鏍囬 | `patchouli:header` | `text`(蹇呴渶), `color`, `centered`, `scale` |
| 瀹炰綋 | `patchouli:entity` | `entity`(蹇呴渶), `render_size`, `rotate` |
| 鍒嗛殧绾?| `patchouli:separator` | 鏃犻澶栧睘鎬?|
| 杈规 | `patchouli:frame` | 涓?200x200 鍥剧墖缁樺埗杈规 |
| 鎻愮ず | `patchouli:tooltip` | `tooltip`(String[]), `width`, `height` |
| 鑷畾涔?| `patchouli:custom` | `class`(瀹炵幇 `ICustomComponent`) |

---

## 浠ｇ爜 API

### IPatchouliAPI
```java
IPatchouliAPI api = PatchouliAPI.get();
if (api.isStub()) { /* Patchouli 鏈畨瑁?*/ return; }
```

| 鏂规硶 | 璇存槑 |
|---|---|
| `getBook()`, `openBookGUI()` | 鑾峰彇/鎵撳紑鎵嬪唽 |
| `registerMultiblock()` | 娉ㄥ唽澶氭柟鍧?|
| `setConfigFlag()` | 璁剧疆 Config flag |
| `getConfigFlag()` | 鏌ヨ Config flag |

### IComponentProcessor
```java
public class MyProcessor implements IComponentProcessor {
    @Override
    public void setup(IPageWrapper page) {
        String text = page.getText("text");
    }
    @Override
    public String process(String key) {
        if ("text".equals(key)) return "澶勭悊鍚庣殑鏂囨湰";
        return null;
    }
}
```

---

## Config Gating

### 榛樿 flags
| Flag | 鏉′欢 |
|---|---|
| `debug` | IDE 璋冭瘯妯″紡 |
| `mod:MODID` | 鏌愭ā缁勫凡鍔犺浇锛屽 `mod:create` |

### 琛ㄨ揪寮?| 琛ㄨ揪寮?| 鍚箟 |
|---|---|
| `!flag` | 鍚﹀畾 |
| `&flag1,flag2,flag3` | 鍏ㄩ儴涓?true锛圓ND锛?|
| `|flag1,flag2,flag3` | 浠讳竴涓?true锛圤R锛?|

鍙敤浜庯細Category銆丒ntry銆丳age銆乀emplate Component 鐨?`flag` 瀛楁銆