---
name: "patchouli-dev"
description: "Patchouli 指南书开发指南。涵盖 book.json 配置、Category/Entry JSON、页面类型、模板系统、代码 API、Config Gating。当需要创建或修改模组指南书时调用本 skill。"
---

# Patchouli 指南书开发指南

## book.json

`/data/<modid>/patchouli_books/<book_name>/book.json`

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `name` | String | ✅ | 手册显示名称 |
| `landing_text` | String | ✅ | 首页文本 |
| `use_resource_pack` | Boolean | - | 1.20 必须为 `true` |
| `book_texture` | String | - | 背景纹理，预设 7 色 |
| `model` | String | - | 物品模型，预设 7 色 |
| `version` | String | - | 版本号 |
| `subtitle` | String | - | 副标题 |
| `i18n` | Boolean | - | 启用 i18n lang key 查找 |
| `pause_game` | Boolean | - | 打开时暂停游戏 |

### 本项目示例
```json
{
  "name": "goetypunk.guide_book.name",
  "landing_text": "goetypunk.guide_book.landing",
  "subtitle": "goetypunk.guide_book.subtitle",
  "version": 1,
  "book_texture": "patchouli:textures/gui/book_purple.png",
  "model": "patchouli:book_purple",
  "use_resource_pack": true
}
```

---

## Category JSON

`/assets/<modid>/patchouli_books/<book_name>/en_us/categories/<name>.json`

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `name` | String | ✅ | 分类名称 |
| `description` | String | ✅ | 分类描述 |
| `icon` | String | ✅ | 图标（ItemStack String 或 .png 纹理） |
| `parent` | String | - | 父分类 ID |
| `flag` | String | - | Config flag 表达式 |
| `sortnum` | Integer | - | 排序号 |
| `secret` | Boolean | - | 隐藏分类 |

```json
{
  "name": "goetypunk.category.machines",
  "description": "GoetyPunk 添加的各类机器",
  "icon": "goetypunk:example_machine",
  "sortnum": 0
}
```

---

## Entry JSON

`/assets/<modid>/patchouli_books/<book_name>/en_us/entries/<category>/<name>.json`

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `name` | String | ✅ | 词条名称 |
| `category` | String | ✅ | 所属分类 |
| `icon` | String | ✅ | 图标 |
| `pages` | Array | ✅ | 页面数组 |
| `priority` | Boolean | - | 置顶 |
| `secret` | Boolean | - | 隐藏 |
| `read_by_default` | Boolean | - | 默认已读 |
| `sortnum` | Integer | - | 排序号 |
| `advancement` | String | - | 锁定进度 |
| `flag` | String | - | Config flag |

---

## 默认 Page Types

所有页面通用字段：`type`(必需)、`advancement`、`flag`、`anchor`。

### text `patchouli:text`
| 字段 | 必需 | 说明 |
|---|---|---|
| `text` | ✅ | 正文，支持格式化 |
| `title` | - | 标题 |

首页必须是 text 页。快捷写法：`"pages": ["这是一段纯文本"]`

### image `patchouli:image`
| 字段 | 必需 | 说明 |
|---|---|---|
| `images` | ✅ | String 数组 |
| `title` | - | 标题 |
| `border` | - | 边框，默认 false |
| `text` | - | 说明文字 |

建议图片 256x256，内容在左上 200x200 区域。

### crafting `patchouli:crafting`
| 字段 | 必需 | 说明 |
|---|---|---|
| `recipe` | ✅ | 配方 ID |
| `recipe2` | - | 第二个配方 ID |
| `text` | - | 说明 |

### smelting `patchouli:smelting`
| 字段 | 必需 | 说明 |
|---|---|---|
| `recipe` | ✅ | 配方 ID |
| `recipe2` | - | 第二个配方 ID |

### multiblock `patchouli:multiblock`
| 字段 | 必需 | 说明 |
|---|---|---|
| `multiblock` | ✅ | 多方块 ID |
| `name` | ✅ | 多方块名称 |
| `text` | - | 说明 |
| `enable_visualize` | - | 可视化按钮 |

### spotlight `patchouli:spotlight`
| 字段 | 必需 | 说明 |
|---|---|---|
| `item` | ✅ | 聚焦物品 |
| `text` | - | 说明 |
| `link_recipe` | - | 点击跳转配方 |

### entity `patchouli:entity`
| 字段 | 必需 | 说明 |
|---|---|---|
| `entity` | ✅ | 实体 ID |
| `text` | - | 说明 |
| `scale` | - | 缩放 |
| `offset` | - | 偏移 |

---

## Template 系统

### 模板 JSON
`/assets/<modid>/patchouli_books/<book_name>/en_us/templates/<name>.json`

```json
{
  "components": [
    { "type": "patchouli:text", "text": "#text", "x": 20, "y": 30 },
    { "type": "patchouli:item", "item": "#item", "x": 80, "y": 10, "link_recipe": true }
  ],
  "processor": "com.goetypunk.addon.jei.MyProcessor"
}
```

前缀 `#` 的为变量，在 entry 中赋值。

### 组件类型
| 类型 | 标识 | 说明 |
|---|---|---|
| 文本 | `patchouli:text` | `text`(必需), `color`, `max_width`, `line_height` |
| 物品 | `patchouli:item` | `item`(必需), `framed`, `link_recipe` |
| 图片 | `patchouli:image` | `image`(必需), `width`, `height`, `u`, `v`, `scale` |
| 标题 | `patchouli:header` | `text`(必需), `color`, `centered`, `scale` |
| 实体 | `patchouli:entity` | `entity`(必需), `render_size`, `rotate` |
| 分隔线 | `patchouli:separator` | 无额外属性 |
| 边框 | `patchouli:frame` | 为 200x200 图片绘制边框 |
| 提示 | `patchouli:tooltip` | `tooltip`(String[]), `width`, `height` |
| 自定义 | `patchouli:custom` | `class`(实现 `ICustomComponent`) |

---

## 代码 API

### IPatchouliAPI
```java
IPatchouliAPI api = PatchouliAPI.get();
if (api.isStub()) { /* Patchouli 未安装 */ return; }
```

| 方法 | 说明 |
|---|---|
| `getBook()`, `openBookGUI()` | 获取/打开手册 |
| `registerMultiblock()` | 注册多方块 |
| `setConfigFlag()` | 设置 Config flag |
| `getConfigFlag()` | 查询 Config flag |

### IComponentProcessor
```java
public class MyProcessor implements IComponentProcessor {
    @Override
    public void setup(IPageWrapper page) {
        String text = page.getText("text");
    }
    @Override
    public String process(String key) {
        if ("text".equals(key)) return "处理后的文本";
        return null;
    }
}
```

---

## Config Gating

### 默认 flags
| Flag | 条件 |
|---|---|
| `debug` | IDE 调试模式 |
| `mod:MODID` | 某模组已加载，如 `mod:create` |

### 表达式
| 表达式 | 含义 |
|---|---|
| `!flag` | 否定 |
| `&flag1,flag2,flag3` | 全部为 true（AND） |
| `|flag1,flag2,flag3` | 任一为 true（OR） |

可用于：Category、Entry、Page、Template Component 的 `flag` 字段。