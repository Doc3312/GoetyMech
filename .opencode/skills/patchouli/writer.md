---
name: patchouli-writer
description: Quick Patchouli entry generator — input item description, output bilingual entry JSON + lang updates.
---

# Patchouli Writer

> 将物品的中文描述快速转化为 Goety black_book 中的双语条目（zh_cn + en_us）。

## 前置条件

先加载本目录的 `SKILL.md` （合并自原 patchouli-dev）获取底层技术细节（page types、entry JSON 格式等）。

## 工作流

### Step 1: 用户输入

用户提供自然语言中文描述，格式示例：

```
育巢 @机械
相较于记录中所记载的那种血腥机器，你重制出了更为模块化，看上去也更美观的机器。
输入任意可恢复饥饿值的食物，即可转化出扭曲胚胎。
```

`@机械` 指定分类，必须为以下之一：`仪式`、`机械`、`聚晶`、`探索`、`工艺`。

### Step 2: AI 查找注册名

搜索 `src/main/java/com/goetymech/addon/init/ModItems.java` 和 `ModBlocks.java`，根据物品中文名推断英文注册名，找到对应的 `"register_name"`。

### Step 3: 推断 page 结构

基于输入文本语义，自动选择合适的 page types 组合（参考 patchouli-dev/page-types.md）：
- 纯描述 → `patchouli:text`
- 展示物品 → `patchouli:spotlight`
- 合成配方 → `patchouli:crafting`（Step 4 询问配方后添加）
- 实体展示 → `patchouli:entity`

**必须主动问用户**："这个物品有合成配方或其他配方吗？请提供配方 ID（如 `goetymech:breeding_nest`），无则回复'无'。"

### Step 4: 生成结构化 JSON

将以上信息组装为以下 JSON 结构，作为 `generate-entry.mjs` 的输入：

```json
{
  "slug": "breeding_nest",
  "category": "machines",
  "name_zh": "育巢",
  "name_en": "Breeding Nest",
  "icon": "goetymech:breeding_nest",
  "sortnum": 0,
  "pages_zh": [
    { "type": "patchouli:text", "text": "相较于记录中所记载的那种血腥机器，你重制出了更为模块化，看上去也更美观的机器。输入任意可恢复饥饿值的食物，即可转化出扭曲胚胎。" }
  ],
  "pages_en": [
    { "type": "patchouli:text", "text": "[AI translated English version]" }
  ],
  "recipes": ["goetymech:breeding_nest"]
}
```

### Step 5: 执行脚本

```bash
node scripts/generate-entry.mjs < input.json
```

脚本自动：
1. 创建 `en_us/entries/<category>/<slug>.json`
2. 创建 `zh_cn/entries/<category>/<slug>.json`
3. 追加分类名和条目名到 lang 文件（en_us.json / zh_cn.json）

## 分类体系

| 分类 ID | 中文名 | sortnum |
|---------|--------|---------|
| `goetymech:goetymech_machines` | 机械 | 8 |
| `goetymech:goetymech_focuses` | 聚晶 | 9 |
| `goetymech:goetymech_rituals` | 仪式 | 10 |
| `goetymech:goetymech_crafts` | 工艺 | 11 |
| `goetymech:goetymech_exploration` | 探索 | 12 |

## 文件路径

```
assets/goetymech/patchouli_books/black_book/
├── en_us/
│   ├── categories/               ← 5个分类 JSON
│   └── entries/goetymech_<cat>/  ← 条目 JSON
├── zh_cn/
│   ├── categories/               ← 5个分类 JSON（中文）
│   └── entries/goetymech_<cat>/  ← 条目 JSON（中文）
```

## 重要规则

- 每次必须问配方，不得跳过
- `sortnum` 从 0 开始递增，按同类物品的推荐阅读顺序分配
- 翻译 en_us 时保持 lore 风格一致，不要过于直译
