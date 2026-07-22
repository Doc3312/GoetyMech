# Third-Party Code Credits

GoetyMech 在开发过程中参考/改编了以下开源项目的代码、算法和架构设计。在此感谢所有原作者的贡献。

---

## 代码/架构参考

| 项目 | 作者 | 许可证 | 使用方式 | 详细说明 |
|------|------|--------|---------|---------|
| **EndLibrary** | MegaDarkness | [MIT](https://opensource.org/licenses/MIT) | 架构改编 | 时停子系统（`TimeStopUtils`、`TimeContext`、`TimeStopSavedData`、`TimeStopUtilsWrapped`、`ClientContext`、`ClientLevelExpandedContext`、`TimeStopRandom`，以及约 12 个 Mixin 文件）的架构设计、变量命名方式和注入策略均改编自 EndLibrary。所有代码已改写并重置于 `com.goetymech.addon.*` 包下，保留了原设计的骨架但要参考原项目请访问 [EndLibrary](https://github.com/meglevel/ending_library) |
| **EpicFight** | Epic Fight Team | [GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.html) | 算法参考 | `SlamGroundTiltHandler.java` 中地面变形旋转/平移公式、`CraterBlockRenderer.java` 中抛物线弹跳动画和 turnback 归位插值算法参考自 EpicFight 的 Fracture 系统。代码为独立实现，注释中已标注来源 |
| **ParCool** | Alrex | [LGPL-3.0](https://www.gnu.org/licenses/lgpl-3.0.html) | 概念参考 | 墙跑系统的设计概念参考自 ParCool。代码（`WallUtil.java`、`PrototypeEvents.java` 墙跑段）为独立重写实现，使用不同的碰撞检测和移动算法 |

---

## 前置依赖（必需）

以下模组为 GoetyMech 的运行时前置依赖，GoetyMech 在其 API 基础上构建：

| 项目 | 作者 | 许可证 |
|------|------|--------|
| **Goety** | Polarice3 | [MIT](https://opensource.org/licenses/MIT) |
| **Create** | The Create Team | [MIT](https://opensource.org/licenses/MIT)（代码）/ All Rights Reserved（资产） |

## 可选依赖（兼容层）

以下模组通过可选兼容层集成，GoetyMech 不直接依赖它们的代码：

| 项目 | 作者 | 许可证 |
|------|------|--------|
| **Curios** | TheIllusiveC4 | [LGPL-3.0](https://www.gnu.org/licenses/lgpl-3.0.html) |
| **JEI** | mezz | [MIT](https://opensource.org/licenses/MIT) |
| **Patchouli** | Vazkii | [MIT](https://opensource.org/licenses/MIT) |

---

GoetyMech 本身采用 ??? 许可证（待确定）。如果你认为本项目对你的作品构成侵权，请联系作者处理。
