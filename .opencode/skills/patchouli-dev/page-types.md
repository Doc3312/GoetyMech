# 默认 Page Types

所有页面通用字段：`type`(必需)、`advancement`、`flag`、`anchor`。

## text `patchouli:text`

| 字段 | 必需 | 说明 |
|---|---|---|
| `text` | ✅ | 正文，支持格式化 |
| `title` | - | 标题（不可用于首页） |

首页必须是 text 页。快捷写法：

```json
"pages": [
  "这是一段纯文本",
  { "type": "patchouli:text", "text": "等效写法" }
]
```

## image `patchouli:image`

| 字段 | 必需 | 说明 |
|---|---|---|
| `images` | ✅ | String 数组，多个时显示翻页箭头 |
| `title` | - | 标题 |
| `border` | - | 边框，默认 false |
| `text` | - | 说明文字 |

建议图片 256x256，内容在左上 200x200 区域。

## crafting `patchouli:crafting`

| 字段 | 必需 | 说明 |
|---|---|---|
| `recipe` | ✅ | 配方 ID |
| `recipe2` | - | 第二个配方 ID |
| `title` | - | 自定义标题（不设则显示产物名） |
| `text` | - | 说明 |

## smelting `patchouli:smelting`

| 字段 | 必需 | 说明 |
|---|---|---|
| `recipe` | ✅ | 配方 ID |
| `recipe2` | - | 第二个配方 ID |
| `title` | - | 自定义标题 |
| `text` | - | 说明 |

## multiblock `patchouli:multiblock`

| 字段 | 必需 | 说明 |
|---|---|---|
| `name` | ✅ | 多方块名称 |
| `multiblock_id` | - | 代码注册的多方块 ID |
| `multiblock` | Object | - 内联定义 |
| `enable_visualize` | - | 可视化按钮，默认 true |
| `text` | - | 说明 |

`multiblock_id` 和 `multiblock` 二选一。

## entity `patchouli:entity`

| 字段 | 必需 | 说明 |
|---|---|---|
| `entity` | ✅ | 实体 ID，可附加 NBT |
| `scale` | - | 缩放，默认 1.0 |
| `offset` | - | 垂直偏移 |
| `rotate` | - | 旋转，默认 true |
| `default_rotation` | - | 默认 -45 |
| `name` | - | 自定义显示名 |
| `text` | - | 说明 |

## spotlight `patchouli:spotlight`

| 字段 | 必需 | 说明 |
|---|---|---|
| `item` | ✅ | 支持 ingredient 对象 / ItemStack String / tag 对象 |
| `title` | - | 自定义标题 |
| `link_recipe` | - | 标记为配方页，默认 false |
| `text` | - | 说明 |

Item 格式示例：

```json
// 原版物品格式
{ "item": { "item": "minecraft:diamond_sword" } }
// Tag 格式
{ "item": { "tag": "minecraft:axes" } }
// ItemStack String
{ "item": "minecraft:diamond_sword,tag:minecraft:axes,minecraft:diamond_shovel" }
```

## link `patchouli:link`

| 字段 | 必需 | 说明 |
|---|---|---|
| `url` | ✅ | URL（仅 HTTP/HTTPS） |
| `link_text` | ✅ | 按钮文字 |

也是 Text 页变体，支持 text 所有字段。

## relations `patchouli:relations`

| 字段 | 必需 | 说明 |
|---|---|---|
| `entries` | - | 关联词条 ID 数组 |
| `title` | - | 标题 |
| `text` | - | 说明 |

## quest `patchouli:quest`

| 字段 | 必需 | 说明 |
|---|---|---|
| `trigger` | - | 完成任务的进度 ID |
| `title` | - | 标题（不设显示 "Objective"） |
| `text` | - | 说明 |

一个 entry 只能有一个 quest 页；配合 `turnin` 字段使用。

## empty `patchouli:empty`

| 字段 | 必需 | 说明 |
|---|---|---|
| `draw_filler` | - | 显示填充纹理，默认 true |
