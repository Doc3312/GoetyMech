# Category JSON

`/assets/<modid>/patchouli_books/<book_name>/en_us/categories/<name>.json`

## 字段表

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `name` | String | ✅ | 分类名称，可用 i18n key |
| `description` | String | ✅ | 分类描述，支持格式化 |
| `icon` | String | ✅ | 图标（ItemStack String 或 .png 纹理） |
| `parent` | String | - | 父分类 ID，格式 `domain:name` |
| `flag` | String | - | Config flag 表达式 |
| `sortnum` | Integer | - | 排序号，默认 0 |
| `secret` | Boolean | - | 隐藏分类，默认 false |

## 示例

```json
{
  "name": "goetypunk.category.machines",
  "description": "GoetyPunk 添加的各类机器",
  "icon": "goetypunk:example_machine",
  "sortnum": 0
}
```

### 子分类

```json
{
  "name": "goetypunk.category.machines.flesh_furnace",
  "description": "血肉熔炉的使用指南",
  "icon": "goetypunk:flesh_furnace",
  "parent": "goetypunk:machines",
  "sortnum": 1
}
```

---

# Entry JSON

`/assets/<modid>/patchouli_books/<book_name>/en_us/entries/<category>/<name>.json`

## 字段表

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `name` | String | ✅ | 词条名称 |
| `category` | String | ✅ | 所属分类 ID |
| `icon` | String | ✅ | 图标 |
| `pages` | Object[] | ✅ | 页面数组 |
| `advancement` | String | - | 锁定进度 ID |
| `flag` | String | - | Config flag 表达式 |
| `priority` | Boolean | - | 置顶，默认 false |
| `secret` | Boolean | - | 隐藏，默认 false |
| `read_by_default` | Boolean | - | 不显示未读标记，默认 false |
| `sortnum` | Integer | - | 排序号 |
| `turnin` | String | - | 完成进度 ID |
| `extra_recipe_mappings` | Object | - | `{ "itemstack_string": pageIndex }` |
| `entry_color` | String | - | 分类列表中词条链接颜色 |

## 示例

```json
{
  "name": "goetypunk.entry.flesh_furnace",
  "category": "goetypunk:machines",
  "icon": "goetypunk:flesh_furnace",
  "advancement": "goetypunk:get_flesh_furnace",
  "pages": [
    { "type": "patchouli:text", "text": "goetypunk.page.flesh_furnace.intro" },
    { "type": "patchouli:crafting", "recipe": "goetypunk:flesh_furnace", "text": "goetypunk.page.flesh_furnace.craft" },
    { "type": "patchouli:spotlight", "item": "goetypunk:cooked_flesh", "link_recipe": true, "text": "goetypunk.page.flesh_furnace.output" }
  ]
}
```
