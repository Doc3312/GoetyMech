status: verified

# Goety 2.5.54.2 TOML 配置重复 spell tables 导致启动崩溃

## 现象

`gradlew runClient` 在 Goety 构造阶段崩溃：

```
com.electronwill.nightconfig.core.io.ParsingException: Table with path [Spells, Blazing Spell] has been declared twice.
```

## 根因

Goety 2.5.54.2 的 `SpellConfig.java` 中有复制粘贴 bug，导致 `ForgeConfigSpec` 自动生成的 `goety-spells.toml` 包含 9 个重复的 `[Spells."Xxx Spell"]` tables：

| 重复的 Spell | 首次出现行 | 重复行 |
|---|---|---|
| Blazing Spell | 1026 | 1202 |
| Blackguard Spell | 934 | 1244 |
| Skull Spell | 971 | 1333 |
| Vexing Spell | 815 | 1369 |
| Ghastly Spell | 1009 | 1386 |
| Phantasm Spell | 900 | 1475 |
| Rotting Spell | 849 | 1644 |
| Wither Skull Spell | 993 | 1661 |
| Bombardment Spell | 1074 | 1716 |

对照 `D:\Libcode\Goety-2-1.20\src\main\java\com\Polarice3\Goety\config\SpellConfig.java` 第 1252 行发现 `BUILDER.push("Quaking Spell")` 本应为 `"Earth Fist Spell"`，但实际生成的文件中 Quaking Spell 未重复而 Blazing Spell 重复，说明 Goety 的 CurseForge 分发版与 Libcode 源码版本有差异。

详见 `fix-goety-toml.mjs` 脚本。

## 触发条件

仅在 `goety-spells.toml` 不存在时（如 `gradlew clean` 后首次启动）触发。已有正常配置时不会复现。

## 修复

```bash
node .opencode/scripts/fix-goety-toml.mjs
```

删除 9 个重复 sections（保留首次出现的第一个）。修复后 `runClient` 正常启动，玩家可进入游戏。

## 教训

- Goety 2.5.54.2 的 config auto-generation 有 bug，`gradlew clean` 会导致游戏无法启动
- 调试时不要轻易 clean，保留 `run/config/` 目录
- 首次崩溃可能是 Goety config 生成 bug，不是我们的代码问题

## 相关文件

- `.opencode/scripts/fix-goety-toml.mjs` — TOML 去重脚本
- `run/config/goety/goety-spells.toml` — 修复后的配置文件
