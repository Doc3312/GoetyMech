# GoetyMech Mod — Domain Context

## 🚫 禁令（所有操作前必须对照）

**1. 回复语言**：无论何种情况（包括上下文压缩、断点续接、状态压缩、plan/build 模式切换），所有发送给用户的文本信息**必须使用中文**。代码内容、日志输出、技术术语保持原文。

**2. 存档保护**：未经用户明确许可，禁止以任何理由、任何方式创建、修改或删除以下内容：
- **存档文件**（Minecraft saves / world data / player data / region files）
- **任何运行时数据文件**（如 `level.dat`、`playerdata/`、`region/` 等）
- 上述行为的触发包括但不限于：测试、清理资源、重置环境、修复 bug、"不小心"覆盖

如需操作存档或运行时数据，必须先询问用户并取得明确许可。

**3. 删除快照保护**：任何文件/目录删除操作，执行前必须自动创建 Git 分支快照（分支名 `backup-N`，N 递增 0~9，超限自动删除最旧）。创建快照后告知用户分支名，再执行删除。参见 `~/.config/opencode/instructions/delete-guard.md`。

**4. 每次修改自动提交**：`edit` / `write` 操作完成后立即执行 `git commit --author="opencode <autocommit>" -m "[auto] <tool>: <filepath> | <diff>"`，用于精准回退单次改动。查看：`git log --author=opencode --oneline`，回退：`git revert <hash>`。

## ⚡ 最高优先级：前置模组源码路径

**所有前置模组的完整源代码在 `D:\Libcode\` 目录下，可直接查阅而非反编译。**

**在反编译任何类之前，必须优先到以下目录查找源码：**

| 源码目录 | 内容 |
|----------|------|
| `D:\Libcode\Goety-2-1.20\Goety-2-1.20\` | Goety 2.x 完整原始码 |
| `D:\Libcode\Create-mc1.20.1-dev\Create-mc1.20.1-dev\` | Create 模組原始码 |
| `D:\Libcode\ending_library-1.20.1\` | 时间停止系统支援库 (ending_library) |

**规则**：任何时候需要了解某个类的行为、查看方法签名、或调试交互逻辑，先去 `D:\Libcode\` 对应目录下搜索源文件。仅在 `D:\Libcode\` 中找不到时才使用反编译。

---

## Project Identity

| 项 | 值 |
|----|-----|
| Mod ID | `GoetyMech` |
| 主包 | `com.GoetyMech.addon` |
| 主入口 | `com.GoetyMech.addon.GoetyMechMod` |
| MC 版本 | 1.20.1 / Forge 47.4.0 / Java 17 |
| 映射 | Parchment 2023.09.03 |
| 前置 | Curios 5.3.4, Goety 2.x, Create 6.0.8 |

---

## 开发测试规范

### 结构化日志格式

所有需要在游戏内验证的功能，在关键操作点输出结构化日志：

```
GoetyMechMod.LOGGER.info("[TEST][<feature-slug>] key=value key=value ...")
```

- `[TEST]` 标记为测试日志，不干扰普通日志阅读
- `[<feature-slug>]` 功能标识（如 `stress-shield`, `void-blink`, `crushing-wheel`）
- `key=value` 结构化数据，用空格分隔

**输出控制**：devMode ON 时 info 级输出，OFF 时 debug 级输出。

### JUnit 5 单元测试

`src/test/java/` 下写纯逻辑测试（不依赖 MC 运行时）。
测试是开发脚手架，验证通过后按用户要求删除。
