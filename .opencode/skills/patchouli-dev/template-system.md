# Template 绯荤粺

## 妯℃澘 JSON

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

鍓嶇紑 `#` 鐨勪负鍙橀噺锛屽湪 entry 涓祴鍊笺€俙include` 宓屽鍏朵粬妯℃澘锛宍processor` 涓轰唬鐮佸鐞嗗櫒銆?

## 缁勪欢绫诲瀷

| 绫诲瀷 | 鏍囪瘑 | 璇存槑 |
|---|---|---|
| 鏂囨湰 | `patchouli:text` | `text`(蹇呴渶), `color`, `max_width`, `line_height` |
| 鐗╁搧 | `patchouli:item` | `item`(蹇呴渶), `framed`, `link_recipe` |
| 鍥剧墖 | `patchouli:image` | `image`(蹇呴渶), `width`, `height`, `u`, `v`, `texture_width`, `texture_height`, `scale` |
| 鏍囬 | `patchouli:header` | `text`(蹇呴渶), `color`, `centered`, `scale` |
| 瀹炰綋 | `patchouli:entity` | `entity`(蹇呴渶), `render_size`, `rotate`, `default_rotation` |
| 鍒嗛殧绾?| `patchouli:separator` | 鏃犻澶栧睘鎬?|
| 杈规 | `patchouli:frame` | 涓?200x200 鍥剧墖缁樺埗杈规 |
| 鎻愮ず | `patchouli:tooltip` | `tooltip`(String[]), `width`, `height` |
| 鑷畾涔?| `patchouli:custom` | `class`(瀹炵幇 `ICustomComponent`) |

鎵€鏈夌粍浠堕€氱敤灞炴€э細`x`, `y`, `advancement`, `negate_advancement`, `guard`, `flag`, `group`銆?

## 浣跨敤妯℃澘鐨?Entry

```json
{
  "type": "GoetyMech:my_template",
  "text": "浼犲叆鐨勬枃鏈唴瀹?,
  "item": "GoetyMech:example_item"
}
```
