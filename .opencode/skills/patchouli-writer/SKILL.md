---
name: patchouli-writer
description: Quick Patchouli entry generator — input item description, output bilingual entry JSON + lang updates.
---

# Patchouli Writer

> 将物品的中文描述快速转化为 Goety black_book 中的双语条目（zh_cn + en_us）。

## 前置条件

先加载 `patchouli-dev` skill 获取底层技术细节（page types、entry JSON 格式等）。

## 工作流

### Step 1: 用户输入

用户提供自然语言中文描述 + `@分类名`，示例：

```
育巢 @机械
相较于记录中所记载的那种血腥机器，你重制出了更为模块化，看上去也更美观的机器。
输入任意可恢复饥饿值的食物，即可转化出扭曲胚胎。
```

`@分类名` 必须为以下之一：`仪式`、`机械`、`聚晶`、`探索`、`工艺`。

### Step 2: AI 查找注册名

搜索 `ModItems.java` 和 `ModBlocks.java`，找到注册名。

### Step 3: 推断 page 结构 + 问配方

基于输入文本选择 page types。**必须主动问用户配方 ID。**

### Step 4: 组装 JSON 并执行脚本

```json
{
  "slug": "breeding_nest",
  "category": "machines",
  "name_zh": "育巢",
  "name_en": "Breeding Nest",
  "icon": "goetymech:breeding_nest",
  "sortnum": 0,
  "pages_zh": [...],
  "pages_en": [...],
  "recipes": ["goetymech:breeding_nest"]
}
```

```bash
node scripts/generate-entry.mjs input.json
```

脚本自动创建 en_us/zh_cn entry JSON 并追加 lang 键。

## 分类体系

| 分类 ID | 中文名 | parent |
|---------|--------|--------|
| `goety:goetymech` | 机械巫法 | (顶层, sortnum 10) |
| `goety:goetymech_rituals` | 仪式 | `goety:goetymech` |
| `goety:goetymech_machines` | 机械 | `goety:goetymech` |
| `goety:goetymech_focuses` | 聚晶 | `goety:goetymech` |
| `goety:goetymech_exploration` | 探索 | `goety:goetymech` |
| `goety:goetymech_crafts` | 工艺 | `goety:goetymech` |

## 文件路径

```
assets/goety/patchouli_books/black_book/
├── en_us/
│   ├── categories/               ← 已有（勿改）
│   └── entries/goetymech_<cat>/  ← 脚本创建
├── zh_cn/
│   ├── categories/               ← 已有（勿改）
│   └── entries/goetymech_<cat>/  ← 脚本创建
```

## 重要规则

- 每次必须问配方，不得跳过
- `sortnum` 从 0 递增，按推荐阅读顺序
- 翻译 en_us 时保持 lore 风格
