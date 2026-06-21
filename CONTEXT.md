# GoetyPunk Mod — Domain Context

## 🚫 禁令（所有操作前必须对照）

**未经用户明确许可，禁止以任何理由、任何方式创建、修改或删除以下内容：**
- **存档文件**（Minecraft saves / world data / player data / region files）
- **任何运行时数据文件**（如 `level.dat`、`playerdata/`、`region/` 等）
- 上述行为的触发包括但不限于：测试、清理资源、重置环境、修复 bug、"不小心"覆盖

如需操作存档或运行时数据，必须先询问用户并取得明确许可。用户说"可以"或"同意"后才允许执行。

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
| Mod ID | `goetypunk` |
| 主包 | `com.goetypunk.addon` |
| 主入口 | `com.goetypunk.addon.GoetyPunkMod` |
| MC 版本 | 1.20.1 / Forge 47.4.0 / Java 17 |
| 映射 | Parchment 2023.09.03 |
| 前置 | Curios 5.3.4, Goety 2.x, Create 6.0.8 |
