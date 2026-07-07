---
status: verified
---

## 现象

编辑 `zh_cn.json` 时整体覆盖了文件，导致之前所有中文翻译丢失（扭曲胚胎、兵团聚晶、应力聚晶、原型、灵魂捕手……）。恢复后发现 commit 4d4ed99（另一 session）就已经干过同样的事。

## 根因

编辑 i18n 文件时没有用 `git log -p -- src/main/resources/assets/GoetyMech/lang/zh_cn.json` 确认文件历史。以为 zh_cn.json 是当前 feature 新建的文件，实际上它历史上已经有完整的多组条目。

## 教训 / 规则

**编辑任何已存在的 i18n / 配置文件前，必须先用 `git show HEAD~1:path/to/file` 或 `git log --oneline -- path` 检查该文件是否有历史遗留内容。不允许假设文件是新的。**

## 关联

- commit 4d4ed99（第一次误删）
- commit aade8a1 和 ac7e0f9（延续了缺失状态）
- commit f378350（恢复）
