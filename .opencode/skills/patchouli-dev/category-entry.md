# Category JSON

`/assets/<modid>/patchouli_books/<book_name>/en_us/categories/<name>.json`

## 瀛楁琛?

| 瀛楁 | 绫诲瀷 | 蹇呴渶 | 璇存槑 |
|---|---|---|---|
| `name` | String | 鉁?| 鍒嗙被鍚嶇О锛屽彲鐢?i18n key |
| `description` | String | 鉁?| 鍒嗙被鎻忚堪锛屾敮鎸佹牸寮忓寲 |
| `icon` | String | 鉁?| 鍥炬爣锛圛temStack String 鎴?.png 绾圭悊锛?|
| `parent` | String | - | 鐖跺垎绫?ID锛屾牸寮?`domain:name` |
| `flag` | String | - | Config flag 琛ㄨ揪寮?|
| `sortnum` | Integer | - | 鎺掑簭鍙凤紝榛樿 0 |
| `secret` | Boolean | - | 闅愯棌鍒嗙被锛岄粯璁?false |

## 绀轰緥

```json
{
  "name": "GoetyMech.category.machines",
  "description": "GoetyMech 娣诲姞鐨勫悇绫绘満鍣?,
  "icon": "GoetyMech:example_machine",
  "sortnum": 0
}
```

### 瀛愬垎绫?

```json
{
  "name": "GoetyMech.category.machines.flesh_furnace",
  "description": "琛€鑲夌啍鐐夌殑浣跨敤鎸囧崡",
  "icon": "GoetyMech:flesh_furnace",
  "parent": "GoetyMech:machines",
  "sortnum": 1
}
```

---

# Entry JSON

`/assets/<modid>/patchouli_books/<book_name>/en_us/entries/<category>/<name>.json`

## 瀛楁琛?

| 瀛楁 | 绫诲瀷 | 蹇呴渶 | 璇存槑 |
|---|---|---|---|
| `name` | String | 鉁?| 璇嶆潯鍚嶇О |
| `category` | String | 鉁?| 鎵€灞炲垎绫?ID |
| `icon` | String | 鉁?| 鍥炬爣 |
| `pages` | Object[] | 鉁?| 椤甸潰鏁扮粍 |
| `advancement` | String | - | 閿佸畾杩涘害 ID |
| `flag` | String | - | Config flag 琛ㄨ揪寮?|
| `priority` | Boolean | - | 缃《锛岄粯璁?false |
| `secret` | Boolean | - | 闅愯棌锛岄粯璁?false |
| `read_by_default` | Boolean | - | 涓嶆樉绀烘湭璇绘爣璁帮紝榛樿 false |
| `sortnum` | Integer | - | 鎺掑簭鍙?|
| `turnin` | String | - | 瀹屾垚杩涘害 ID |
| `extra_recipe_mappings` | Object | - | `{ "itemstack_string": pageIndex }` |
| `entry_color` | String | - | 鍒嗙被鍒楄〃涓瘝鏉￠摼鎺ラ鑹?|

## 绀轰緥

```json
{
  "name": "GoetyMech.entry.flesh_furnace",
  "category": "GoetyMech:machines",
  "icon": "GoetyMech:flesh_furnace",
  "advancement": "GoetyMech:get_flesh_furnace",
  "pages": [
    { "type": "patchouli:text", "text": "GoetyMech.page.flesh_furnace.intro" },
    { "type": "patchouli:crafting", "recipe": "GoetyMech:flesh_furnace", "text": "GoetyMech.page.flesh_furnace.craft" },
    { "type": "patchouli:spotlight", "item": "GoetyMech:cooked_flesh", "link_recipe": true, "text": "GoetyMech.page.flesh_furnace.output" }
  ]
}
```
