---
name: patchouli-dev
description: Patchouli / guide book / page
---

# Patchouli 寮€鍙戞寚鍗?(Forge 1.20.1 / Patchouli 1.20.1-85-FORGE)

> 蹇€熸帓闄わ細褰撳墠浠诲姟濡傛灉涓嶆秹鍙?guide book 鍒涘缓銆乸age type 缂栧啓銆乼emplate 鎴?recipe page锛岃璺宠繃鏈?skill銆?
## 鐩綍缁撴瀯

```
src/main/resources/
鈹溾攢鈹€ assets/<modid>/patchouli_books/<book_name>/
鈹?  鈹溾攢鈹€ en_us/categories/
鈹?  鈹溾攢鈹€ en_us/entries/
鈹?  鈹溾攢鈹€ en_us/templates/
鈹?  鈹斺攢鈹€ zh_cn/           (缈昏瘧瑕嗙洊)
鈹斺攢鈹€ data/<modid>/patchouli_books/<book_name>/
    鈹斺攢鈹€ book.json
```

閲嶈锛歜ook.json 鏀惧湪 data/ 涓嬶紝鍐呭鏂囦欢鍦?assets/ 涓嬶紝book.json 椤昏缃?`"use_resource_pack": true`銆?
鏈」鐩細鎵嬪唽鍐呴儴鍚?`GoetyMech_guide`锛屽畬鏁?ID `GoetyMech:GoetyMech_guide`

## 鏂囨。绱㈠紩

| 鏂囦欢 | 鍐呭 |
|------|------|
| [book-json.md](book-json.md) | book.json 瀛楁琛?+ 绀轰緥 + 鏂囨湰鏍煎紡鍖?+ ItemStack String 鏍煎紡 |
| [category-entry.md](category-entry.md) | Category JSON + Entry JSON 瀛楁琛?+ 绀轰緥 |
| [page-types.md](page-types.md) | 11 绉嶉粯璁?Page Type 鐨?JSON 缁撴瀯 + 绀轰緥 |
| [template-system.md](template-system.md) | Template JSON + 缁勪欢绫诲瀷 + 浣跨敤绀轰緥 |
| [code-api.md](code-api.md) | IPatchouliAPI / IComponentProcessor / ICustomComponent / Multiblock / 鏂版墜鎵嬪唽 |
| [config-gating.md](config-gating.md) | Config Flag 琛ㄨ揪寮?+ 鏉′欢鎺у埗绀轰緥 |

## 渚濊禆婧愪唬鐮佽矾寰?
鎵€鏈夊墠缃ā缁勭殑瀹屾暣婧愪唬鐮佸湪 `D:\Libcode\` 鐩綍涓嬶紝鍙洿鎺ユ煡闃呰€岄潪鍙嶇紪璇戯細

| 婧愮爜鐩綍 | 鍐呭 |
|----------|------|
| `D:\Libcode\Goety-2-1.20\Goety-2-1.20\` | Goety 2.x 鍘熷鐮?|
| `D:\Libcode\Create-mc1.20.1-dev\Create-mc1.20.1-dev\` | Create 妯＄祫鍘熷鐮?|

---

## 鐗堟湰瀵圭収

| Patchouli 鐗堟湰 | MC 鐗堟湰 |
|---|---|
| 1.20.1-85-FORGE | 1.20.1 |
