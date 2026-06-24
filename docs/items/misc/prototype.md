# 原型 — goetypunk

## 身份

- 类型：elytra
- 注册类：ModItems.PROTOTYPE
- Java 类：PrototypeItem (extends ElytraItem)
- 首次创建：2026-06-14

## 设计意图

多功能战斗鞘翅。提供：自定义飞行物理（velocity-set 模型）、墙跑系统、蓄力跳跃、Slam 冲击技能。核心玩法是"飞行机动 + 近战压杀"。

## 演化记录

### 2026-06-23 — 墙跑全量旋转实现 + 物理失败归档

- 改动：实现相机全量旋转（四元数 SLERP 平滑过渡）、转角自动检测（findBestWallDirection）、墙面摩擦检测（slipperiness ≥ 0.8 禁止跑墙）、跑墙激活条件放宽（地面冲刺也可激活）；物理层改用 `player.move() + setDeltaMovement(ZERO)` 模型，但仍无法产生实际位移
- 原因：相机旋转需要完整的墙壁=地面视觉体验；物理层 `move()` 执行但 Y 坐标不变化，开放问题待后续诊断
- 关联：wall-run-full-rotation change（已归档）

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

### 2026-06-23 — 墙跑方向计算从视线投影改为水平沿墙方向

- 改动：sprint 时用 `down × wallN` 计算水平沿墙方向，替代 `getLookAngle()` 投影；新增 C2SWallInputPacket（客户端 → 服务端同步 forward/strafe 输入）；墙跑时 `forward=1` fallback
- 原因：视线投影在墙面上产生不可预测的斜向移动，不满足"水平沿墙跑"的预期
- 关联：wall-run iteration (commits c544224 → 565daa0 → 136150e → 5faa6a5)

### 2026-06-23 — 墙跑物理改为直接 move() 绕过 travel() 摩擦

- 改动：取消 setDeltaMovement 被动物理，改为 `player.move(MoverType.SELF, delta)` 直接驱动；delta 清零防止 travel() 覆盖；移除 sprint 检查使墙跑在 sprint 取消后持续
- 原因：travel() 的摩擦覆盖 + onGround 检测导致墙跑速度被大幅衰减，需要绕过原版移动流水线
- 关联：wall-run iteration (commits 1f5853d → c755f7e → cd816fd)

### 2026-06-23 — 墙跑启动时抬升离地 + onGround 守卫

- 改动：墙跑激活时强制抬升 0.2 格（避免地面摩擦抵消水平速度）；在 applyWallMovement 入口加 `player.onGround()` 守卫
- 原因：墙跑从地面开始时，travel() 的地面摩擦立即将速度降到零，需要空中启动
- 关联：commit 9ac4f32

### 2026-06-23 — 墙跑改用视线方向 + 反重力模式

- 改动：墙跑方向改回视线方向（`getLookAngle()`），移除 lift-off-ground；改为持续设置 `setNoGravity(true)` 消除重力影响
- 原因：lift-off-ground 在复杂地形不可靠；反重力比抬升更稳定
- 关联：commit 44f9fb3

### 2026-06-23 — 垂直墙跑实现：W 爬升 / S 下滑 / A D 水平横移

- 改动：墙跑方向从水平沿墙（`forwardOnWall`）改为垂直向上（`wallUp`）：W=爬升、S=下滑、A/D=左右横移；重力从墙面投影改为拉向墙面（`wallN * -WALL_GRAVITY`）；新增 `WALL_PULL_SPEED` 保持贴墙
- 原因：水平沿墙跑不符合 Titanfall 式垂直墙跑的预期；需要 W/S 控制升降、A/D 控制水平横移
- 关联：commit 99b7245（PrototypeRenderEvents.java client-side setDeltaMovement）；Config keys: `WALL_RUN_SPEED`, `WALL_GRAVITY`, `WALL_PULL_SPEED`

### 2026-06-23 — Plan B: Mixin into Player.travel() for wall-run movement

- 改动：HEAD injection 取消 travel()，通过 accessor 设置 onGround=true，手动调用 player.move() 传入墙跑速度（W=wallUp, A/D=wallRight，重力拉向墙面 + 贴墙拉力）；通过 ClientWallRunState 重新应用 sprint；临时 onGround 覆盖实现 step-up
- 原因：travel() 的摩擦模型 + onGround 检测导致取消 sprint 后墙跑速度被大幅衰减；需要完全绕过原版移动流水线以达到 Titanfall 式垂直墙跑效果
- 关联：commit 7a632bf；文件：MixinWallRunTravel.java、ClientWallRunState.java、EntityOnGroundAccessor.java、PrototypeRenderEvents.java

### 2026-06-24 — 墙跑头顶翻越（outward burst + grace period）+ 翻越后 sprint 保持

- 改动：墙跑遇到头顶障碍时自动向外爆发（outward burst 0.9/1.2/0.7/0.5）尝试翻越，成功设 3 tick grace 窗口用 pull 重新贴墙；翻越后 sprintRetainTicks=30（1.5秒）保持冲刺状态；Shift 键退出墙跑
- 原因：原版 travel() 遇天花板碰撞无法继续爬墙；LocalPlayer.aiStep() 在 travel() 后清除 sprint，翻越后 sprint 丢失
- 关联：MixinWallRunTravel.java, ClientWallRunState.java, PrototypeRenderEvents.java; sprintRetain 由用户手动从 10 改为 30 tick

### 2026-06-24 — Sprint cut-off 修复 + 水下墙跑守卫 + 游泳加速重写

- 改动：移除 shouldSprint() 守卫（恢复直接按键轮询）；水下禁止墙跑（移动+相机）；游泳加速从 attribute modifier 改为 mixin 公式 `m = 1.25 - 0.25/d`，三轴乘法保留俯仰角
- 原因：sprint 在翻越后断掉（shouldSprint guard 阻止 sprintDown 刷新）；水下墙跑不应触发；MOVEMENT_SPEED 属性在游泳时无 Depths Strider 时不生效，attribute 方法不可行
- 关联：sprint-cutoff-fix, underwater-wallrun-guard, swim-accel-rework

### 2026-06-25 — 同化击杀计数成长系统

- 改动：新增同化击杀计数系统（PersistentData `goetypunk.assimilation_kills`），4 阶能力解锁（<10=基础机动+滑翔 ≥10=二级加速 ≥50=Slam ≥100=终阶），边际收益伤害倍率函数（1-100k+ 无极 cap），病毒附加伤害（indirectMagic per hit），Shift 展开 tooltip 显示能力列表/状态/统计
- 原因：为 Prototype 提供成长的 RPG 要素，击杀越多能力越强
- 关联：S2CAssimilationDataPacket.java, PrototypeItem.java (tier/damage utils), PrototypeEvents.java (kill tracking, login sync, damage mult), PrototypeTravelMixin.java (飞行/游泳 tier scaling), MixinWallRunTravel.java (墙跑 tier scaling), PrototypeRenderEvents.java (tooltip), zh_cn.json / en_us.json

### 2026-06-23 — 体线粒子系统重构：6 锚点 + 下砸 FLAME 火花

- 改动：移除旧冲刺粒子（DustParticleOptions 随机散布）和蓄力跳粒子（CRIT + ENCHANTED_HIT）；新增 6 身体锚点粒子系统（左手/右手外侧、头顶左/右、左脚/右脚外侧）；S2CSlamStatePacket 同步下砸客户端状态；下砸时生成深红体线 + FLAME 火星 + 淡红尾迹三层混合
- 原因：粒子位置更细致，身体各部位各有尾迹；下砸视觉区分度不足
- 关联：particle-rework change
