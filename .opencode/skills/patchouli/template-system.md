# Template 系统

## 模板 JSON

`/assets/<modid>/patchouli_books/<book_name>/en_us/templates/<name>.json`

```json
{
  "components": [
    {
      "type": "patchouli:text",
      "text": "#text",
      "x": 20,
      "y": 30
    },
    {
      "type": "patchouli:item",
      "item": "#item",
      "x": 80,
      "y": 10,
      "link_recipe": true
    }
  ],
  "include": [],
  "processor": "com.GoetyMech.addon.jei.MyProcessor"
}
```

前缀 `#` 的为变量，在 entry 中赋值。`include` 嵌套其他模板，`processor` 为代码处理器。

## 组件类型

| 类型 | 标识 | 说明 |
|---|---|---|
| 文本 | `patchouli:text` | `text`(必需), `color`, `max_width`, `line_height` |
| 物品 | `patchouli:item` | `item`(必需), `framed`, `link_recipe` |
| 图片 | `patchouli:image` | `image`(必需), `width`, `height`, `u`, `v`, `texture_width`, `texture_height`, `scale` |
| 标题 | `patchouli:header` | `text`(必需), `color`, `centered`, `scale` |
| 实体 | `patchouli:entity` | `entity`(必需), `render_size`, `rotate`, `default_rotation` |
| 分隔线 | `patchouli:separator` | 无额外属性 |
| 边框 | `patchouli:frame` | 为 200x200 图片绘制边框 |
| 提示 | `patchouli:tooltip` | `tooltip`(String[]), `width`, `height` |
| 自定义 | `patchouli:custom` | `class`(实现 `ICustomComponent`) |

所有组件通用属性：`x`, `y`, `advancement`, `negate_advancement`, `guard`, `flag`, `group`。

## 使用模板的 Entry

```json
{
  "type": "GoetyMech:my_template",
  "text": "传入的文本内容",
  "item": "GoetyMech:example_item"
}
```
