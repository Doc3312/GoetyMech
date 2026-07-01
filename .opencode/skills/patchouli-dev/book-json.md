# book.json

`/data/<modid>/patchouli_books/<book_name>/book.json`

## 字段表

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `name` | String | ✅ | 手册显示名称，可用 i18n key |
| `landing_text` | String | ✅ | 首页文本，可用 i18n key，支持格式化 |
| `use_resource_pack` | Boolean | - | 1.20 必须为 `true` |
| `book_texture` | String | - | 背景纹理，预设 7 色 |
| `filler_texture` | String | - | 空白页填充纹理 |
| `crafting_texture` | String | - | 合成页面背景纹理 |
| `model` | String | - | 物品模型，预设 7 色 |
| `text_color` | String | - | 正文颜色 hex "RRGGBB"，默认 "000000" |
| `header_color` | String | - | 标题颜色，默认 "333333" |
| `nameplate_color` | String | - | 首页书名颜色，默认 "FFDD00" |
| `link_color` | String | - | 链接颜色，默认 "0000EE" |
| `link_hover_color` | String | - | 悬停链接颜色，默认 "8800EE" |
| `progress_bar_color` | String | - | 进度条颜色，默认 "FFFF55" |
| `progress_bar_background` | String | - | 进度条背景色，默认 "DDDDDD" |
| `open_sound` | String | - | 打开书音效 resource location |
| `flip_sound` | String | - | 翻页音效 resource location |
| `index_icon` | String | - | 索引图标 |
| `pamphlet` | Boolean | - | 小册子模式 |
| `show_progress` | Boolean | - | 显示进度条，默认 true |
| `version` | String | - | 版本号，显示 "X Edition" |
| `subtitle` | String | - | 副标题 |
| `creative_tab` | String | - | 创造模式标签页 |
| `advancements_tab` | String | - | 关联的进度标签页 ID |
| `dont_generate_book` | Boolean | - | 不自动生成物品 |
| `custom_book_item` | String | - | 自定义书物品 |
| `show_toasts` | Boolean | - | 新词条 toast，默认 true |
| `use_blocky_font` | Boolean | - | 原版像素字体，默认 false |
| `i18n` | Boolean | - | 启用 i18n lang key 查找，默认 false |
| `macros` | Object | - | 自定义格式化宏 |
| `pause_game` | Boolean | - | 打开时暂停游戏，默认 false |
| `text_overflow_mode` | Enum | - | overflow/resize/truncate |

## 本项目示例

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
GoetyMech.guide_book.name=GoetyMech 指南
GoetyMech.guide_book.landing=$(bold)Welcome!$(br2)欢迎查阅 GoetyMech 模组的手册。
GoetyMech.guide_book.subtitle=暗黑工艺指南
```

---

# 文本格式化

`$(...)` 格式码系统。

## 格式化码

| 码 | 说明 |
|---|---|
| `$()` | 清除所有格式 |
| `$(br)` | 换行 |
| `$(br2)` / `$(p)` | 段落 |
| `$(li)` | 列表项（可叠加数字 `$(li2)`） |
| `$(#rrggbb)` | 十六进制颜色 |
| `$(#rgb)` | 缩写色 |
| `$(0)`-`$(f)` | MC 颜色码 |
| `$(k)`/`$(l)`/`$(m)`/`$(n)`/`$(o)` | 混淆/加粗/删除线/下划线/斜体 |
| `$(l:ID)` | 内部链接 |
| `$(l:entryid#anchor)` | 锚点链接 |
| `$(l:https://...)` | 外部链接 |
| `$(/l)` | 结束链接 |
| `$(t:tooltip)` | 悬停提示 |
| `$(/t)` | 结束 tooltip |
| `$(c:/command)` | 点击执行命令 |
| `$(/c)` | 结束命令 |
| `$(playername)` | 玩家名 |
| `$(k:keyname)` | 按键名 |

## 默认宏

| 宏 | 展开 |
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

## 自定义宏

在 `book.json` 的 `macros` 中定义：

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

# ItemStack String 格式

## 写法

| 写法 | 示例 |
|---|---|
| `namespace:path` | `"minecraft:stone"` |
| `namespace:path#count` | `"minecraft:stone#64"` |
| `namespace:path{nbt}` | `"minecraft:diamond_pickaxe{...}"` |
| `namespace:path#count{nbt}` | 组合 |
| `tag:namespace:tag_id` | `"tag:minecraft:axes"` |

多物品用逗号分隔：`"minecraft:sword,tag:minecraft:axes,minecraft:stone#64"`

NBT 中引号可用单引号 `'` 或转义 `\"`。
