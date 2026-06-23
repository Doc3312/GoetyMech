# 原型 — goetypunk

## 身份

- 类型：elytra
- 注册类：ModItems.PROTOTYPE
- Java 类：PrototypeItem (extends ElytraItem)
- 首次创建：2026-06-14

## 设计意图

多功能战斗鞘翅。提供：自定义飞行物理（velocity-set 模型）、墙跑系统、蓄力跳跃、Slam 冲击技能。核心玩法是"飞行机动 + 近战压杀"。

## 演化记录

### 2026-06-18 — 墙跑物理修复：Y 轴独立控制

- 改动：applyWallMovement() 不再继承玩家下落速度，Y 轴设为独立慢滑速度 WALL_SLIDE_SPEED；视线方向保留 Y 分量支持仰头爬墙；退出条件从不冲刺改为落地
- 原因：原代码继承玩家当前速度的 Y 分量（-0.08/tick），乘摩擦后仍以重力速度坠落
- 关联：wallrun-physics-fix issue

### 2026-06-18 — 删除墙跑跳离、飞行加速功能

- 放弃方案：handleWallJumpOff + C2SWallJumpOffPacket、STORED_ZZA 字段飞行加速、地面起飞空中强制飞行
- 放弃原因：墙跑跳离与蓄力跳冲突（共享按键绑定）；飞行加速被 velocity-set 模型替代
- 关联：wallrun-physics-fix issue

### 2026-06-18 — 飞行物理从加法加速器迁移到 velocity-set 模型

- 改动：飞行加速从加法叠加改为每次直接设置合成后的最终速度
- 原因：加法加速器在客户端-服务端同步中存在 forward 方向不一致问题
- 关联：prototype-flight-mixin skill
