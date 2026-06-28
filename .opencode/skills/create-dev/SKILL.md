---
name: create-dev
description: Create machines / Ponder / kinetic BE / BESR
---

# Create 附属开发指南 (Forge 1.20.1 / Create 6.0.8)

> 快速排除：当前任务如果不涉及 Create 机器注册、Ponder、KineticBE、BESR 或应力系统，请跳过本 skill。

## 适用场景

- 配置 Create 附属开发环境
- 使用 CreateRegistrate 注册内容
- 实现 ProcessingRecipe 自定义配方
- 编写 Ponder 场景文档
- 接入 Create API 扩展点
- 理解 KineticBlockEntity 运行时行为（tick/speed/stress/NBT/Rotation Propagation）
- 实现 BESR / Flywheel Visual 自定义渲染
- 编写 Block Model JSON / 配置 blockstate 旋转

## 文档索引

| 文件 | 内容 |
|------|------|
| [dependency-config.md](dependency-config.md) | build.gradle / gradle.properties / mods.toml / Mixin refmap |
| [registrate-registration.md](registrate-registration.md) | Tags 表 + Registrate 注册(Block/Item/BE/Fluid) + DataGen + Stress |
| [processing-ponder-api.md](processing-ponder-api.md) | ProcessingRecipe + Ponder + API 扩展点 |
| [kinetics-runtime.md](kinetics-runtime.md) | KineticBlockEntity 生命周期 / GeneratingBE / RotationPropagator / KineticNetwork / DirectionalKineticBlock / Config |
| [rendering-pipeline.md](rendering-pipeline.md) | BESR 管线(SafeBlockEntityRenderer / KineticBlockEntityRenderer) / Flywheel interplay / CachedBuffers / PartialModel / 标准渲染模式 / Block Model UV 惯例 |

## 本项目当前配置

| 依赖 | 版本 |
|---|---|
| Create | 6.0.8-289 |
| Ponder | 1.0.91 |
| Flywheel API | 1.0.5 (compileOnly) |
| Flywheel Full | 1.0.5 (runtimeOnly) |
| Registrate | MC1.20-1.3.3 |
| MixinExtras | 0.4.1 |

**mods.toml** 中 `create` 已声明为强制依赖，版本范围 `[6.0,)`。

> 前置模组源码路径见 `.opencode/skills/_shared/libcode-paths.md`。

## ⚠️ 容器兼容注意事项

Create 漏斗/传送带通过 `IItemHandler` 与容器交互。当你实现 `WorldlyContainer` 时：

- `SidedInvWrapper` 在**空槽满额接受**路径中传原始 stack 引用
- `setItem()` 中**禁止**用 `stack.split(n)`，会让 Create 传送带侧数据损坏
- 改用 `stack.copyWithCount(n)`

**详见** `goety-addon-dev/forge-basics.md` →「⚠️ 容器开发陷阱」章节。

## 版本对照

| Create | MC | Ponder | Flywheel | Registrate |
|---|---|---|---|---|
| 6.0.8 | 1.20.1 | 1.0.91 | 1.0.5 | MC1.20-1.3.3 |
| 6.0.7 | 1.20.1 | — | — | — |
| 6.0.6 | 1.20.1 | — | — | — |
| 6.0.2 | 1.20.1 | — | — | — |
| 6.0.0 | 1.20.1 | — | — | — |
