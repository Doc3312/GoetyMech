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

### 2026-06-23 — 蓄力跳冲刺惯性修复 + 速度倍率

- 改动：新增 cj_start_speed NBT 保留蓄力开始时客户端速度；CJ 注入不再被 isFallFlying() 阻塞，飞行中持续维持水平速度；在 applyChargedJump 中给 bestSpeed 乘 2.5 倍
- 原因：长蓄力（80+ tick）后 isSprinting() 丢失导致 attrSpeed 塌缩到 0.1；进入飞行后 CJ 注入被跳过，水平速度仅靠 elytra 物理维持（摩擦大）；用户反馈速度仍需提升
- 关联：fix-charge-jump-hspeed change

### 2026-06-23 — 修复 sprint 停止后 groundSpeed 残值导致无输入蓄力跳获得错误水平动量

- 改动：在 sprint 停止分支（`!player.isSprinting()`）中清空 `groundSpeed(NBT)`
- 原因：sprint 期间每 tick 追踪的实测速度残留在 NBT 中，停止 sprint 后未被清除，蓄力跳时 `bestSpeed` 取到旧 sprint 速度，在无 W 输入时产生不应有的水平推力
- 关联：codebase-remediation change

### 2026-06-23 — 体线粒子系统重构：6 锚点 + 下砸 FLAME 火花

- 改动：移除旧冲刺粒子（DustParticleOptions 随机散布）和蓄力跳粒子（CRIT + ENCHANTED_HIT）；新增 6 身体锚点粒子系统（左手/右手外侧、头顶左/右、左脚/右脚外侧）；S2CSlamStatePacket 同步下砸客户端状态；下砸时生成深红体线 + FLAME 火星 + 淡红尾迹三层混合
- 原因：粒子位置更细致，身体各部位各有尾迹；下砸视觉区分度不足
- 关联：particle-rework change
