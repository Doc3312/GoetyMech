---
name: patchouli-dev
description: Patchouli / guide book / page
---

# Patchouli 开发指南 (Forge 1.20.1 / Patchouli 1.20.1-85-FORGE)

> 快速排除：当前任务如果不涉及 guide book 创建、page type 编写、template 或 recipe page，请跳过本 skill。

## 目录结构

```
src/main/resources/
├── assets/<modid>/patchouli_books/<book_name>/
│   ├── en_us/categories/
│   ├── en_us/entries/
│   ├── en_us/templates/
│   └── zh_cn/           (翻译覆盖)
└── data/<modid>/patchouli_books/<book_name>/
    └── book.json
```

重要：book.json 放在 data/ 下，内容文件在 assets/ 下，book.json 须设置 `"use_resource_pack": true`。

本项目：手册内部名 `GoetyMech_guide`，完整 ID `GoetyMech:GoetyMech_guide`

## 文档索引

| 文件 | 内容 |
|------|------|
| [book-json.md](book-json.md) | book.json 字段表 + 示例 + 文本格式化 + ItemStack String 格式 |
| [category-entry.md](category-entry.md) | Category JSON + Entry JSON 字段表 + 示例 |
| [page-types.md](page-types.md) | 11 种默认 Page Type 的 JSON 结构 + 示例 |
| [template-system.md](template-system.md) | Template JSON + 组件类型 + 使用示例 |
| [code-api.md](code-api.md) | IPatchouliAPI / IComponentProcessor / ICustomComponent / Multiblock / 新手手册 |
| [config-gating.md](config-gating.md) | Config Flag 表达式 + 条件控制示例 |

## 依赖源代码路径

所有前置模组的完整源代码在 `D:\Libcode\` 目录下，可直接查阅而非反编译：

| 源码目录 | 内容 |
|----------|------|
| `D:\Libcode\Goety-2-1.20\Goety-2-1.20\` | Goety 2.x 原始码 |
| `D:\Libcode\Create-mc1.20.1-dev\Create-mc1.20.1-dev\` | Create 模組原始码 |

---

## 版本对照

| Patchouli 版本 | MC 版本 |
|---|---|
| 1.20.1-85-FORGE | 1.20.1 |
