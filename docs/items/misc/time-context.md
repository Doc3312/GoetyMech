# TimeContext — goetypunk

## 身份

- 类型：utility
- Java 类：TimeContext (com.goetypunk.addon.common.time)
- 首次创建：2026-06-14

## 设计意图

时停系统的上下文工具类，提供客户端/服务端共享的时间工具方法。
包含 `safeClientFrameTime()`、`Client` 内部类（时停 UI 计时）、`Both` 内部类（双端一致时间源）。

## Timeline

- **2026-06-27** — 修复 C3: `safeClientFrameTime()` 添加 `@OnlyIn(Dist.CLIENT)` 防止 DS 崩溃。
