# 原型 — GoetyMech

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

### 2026-06-25 — 同化击杀计数成长系统（滑翔改为初始能力 + 文件缓存防崩溃丢失）

- 改动：滑翔改为始终可用（hasFlight 返回 true）；新增 AssimilcationSaveManager 每 60s 写独立缓存文件到 playerdata/，写时先写 .tmp 再原子重命名；登录时 cache > NBT 自动恢复击杀数
- 原因：用户发现鞘翅无法禁用滑翔；经历 crash 导致击杀数清零的教训，需要独立于 NBT 的备份机制

### 2026-06-25 — 同化击杀计数成长系统

- 改动：新增同化击杀计数系统（PersistentData `GoetyMech.assimilation_kills`），4 阶能力解锁（<10=基础机动+滑翔 ≥10=二级加速 ≥50=Slam ≥100=终阶），边际收益伤害倍率函数（1-100k+ 无极 cap），病毒附加伤害（indirectMagic per hit），Shift 展开 tooltip 显示能力列表/状态/统计
- 原因：为 Prototype 提供成长的 RPG 要素，击杀越多能力越强
- 关联：S2CAssimilationDataPacket.java, PrototypeItem.java (tier/damage utils), PrototypeEvents.java (kill tracking, login sync, damage mult), PrototypeTravelMixin.java (飞行/游泳 tier scaling), MixinWallRunTravel.java (墙跑 tier scaling), PrototypeRenderEvents.java (tooltip), zh_cn.json / en_us.json

### 2026-06-25 — Mimic 合成体系 + 不可摧毁守卫 + 统一检测

- 改动：新增 `isPrototype(ItemStack)` / `isPrototypeItem(Item)` / `hasAnyPrototype()` 统一检测方法；ItemEntityPrototypeMixin 免疫伤害；ItemEntityVoidMixin 虚空拉回 Y=260；PrototypeMimicItem `createFrom()` 合成继承属性；MimicCombinationRecipe + MimicDeconstructionRecipe 注册
- 原因：prototype 物品需要统一的归属检测接口；mimic 合成系统允许将 prototype+ 胸甲合成为可穿戴 ElytraItem

### 2026-06-25 — 皮下护甲：击杀成长型免伤系统

- 改动：新增 `getDamageImmunityThreshold(int kills)` 三段公式计算免伤阈值；`getNextImmunityMilestone(int kills)` 计算下次进化所需击杀；`LivingHurtEvent` 中受害者免伤判定（有来源伤害且 `amount <= threshold` 时 cancel 事件，播放 EXP_ORB 叮声）；tooltip 子弹俯冲后新增"皮下护甲"行（红/深灰），攻击力加成行末尾追加"伤害免疫：X"
- 公式：`<150=0, 150~1000=kills/100, 1000~10000=10+(kills-1000)/1000, 10000+=19+(kills-10000)/2000`
- 原因：为 prototype 提供与额外生命/护盾不同的第三类防御机制；低伤害被完全无视，随击杀增长阈值提高
- 教训：初始 `(kills-150)/200+1` 增速太慢（200 杀才+1），用户认为含金量不足，改为 `kills/100` 后每 100 击杀稳定+1
- 关联：PrototypeItem.java (formula), PrototypeEvents.java (immunity check), PrototypeRenderEvents.java (tooltip)

### 2026-06-23 — 体线粒子系统重构：6 锚点 + 下砸 FLAME 火花

- 改动：移除旧冲刺粒子（DustParticleOptions 随机散布）和蓄力跳粒子（CRIT + ENCHANTED_HIT）；新增 6 身体锚点粒子系统（左手/右手外侧、头顶左/右、左脚/右脚外侧）；S2CSlamStatePacket 同步下砸客户端状态；下砸时生成深红体线 + FLAME 火星 + 淡红尾迹三层混合
- 原因：粒子位置更细致，身体各部位各有尾迹；下砸视觉区分度不足
- 关联：particle-rework change

### 2026-07-03 — 物品图标纹理内容居中修正

- 改动：将 prototype.png 非透明像素在 16×16 帧内下移右移 1px，内容中心从 (6.5, 6.0) 对齐到 (7.5, 7.0)
- 原因：贴图更新后内容位于帧左上角，`item/generated` 映射后图标在背包中偏左上角显示
- 关联：commit 4745416

### 2026-07-10 — 墙跑重构：客户端主导运动 + 渐进加速 + 服务端不干预

- 改动：MixinWallRunTravel 分离客户端（完整墙跑运动+渐进加速+Y轴控制+wallN归一化）和服务端（仅cancel travel跳过重力，不设速度不hurtMarked）；渐进加速 `speed = min(base + accelRate×wallTicks, base×maxMultiplier×tierMult)` 像飞行/冲刺；Y轴 W=爬(speed)、S=滑(-speed×0.5)、不按=慢滑(-0.04)；C2SWallInputPacket 加 `wallRunning` flag + `wallNormal` 字段，客户端通知道服务端墙跑状态；服务端 `handleWallRunTick` 简化只保留基本退出条件（shift/water/lava），不再自行检测墙（客户端主导）；新增 Config：`WALL_RUN_ACCEL_RATE`(0.02)、`WALL_RUN_MAX_MULTIPLIER`(3.0)
- 原因：服务端墙检测 95%+ 失败（服务端 zza 永远为 0，位置与客户端不同步），且 tierMult 两端不同步导致墙跑速度 3 倍差异
- 关联：diagnose skill；日志分析确认 tierMult desync 为"异常跃起"根因

### 2026-07-10 — 删除 Curios 支持（原型仅限胸甲栏）

- 改动：删除 `PrototypeCurio.java` / `CaelusFlightHandler.java` / `CaelusLoaded.java` / `CuriosHelper.java`；`hasAnyPrototype()` 简化为只查 `EquipmentSlot.CHEST`；从 `data/curios/tags/items/back.json` 移除 `prototype`/`prototype_mimic`，从 `body.json` 移除 `prototype`
- 原因：原型设计目标为仅胸甲栏使用；Curios 适配引入不必要的复杂度（Caelus 依赖、PersistentData 同步问题）
- 教训：先差点删了 BODY/BACK 槽位注册（影响其他 Curios 物品），正确方案是仅移除 tag JSON 中的条目；Curios 物品-槽位映射 100% 基于 Item Tag（`curios:*` 命名空间），不是 capability 也不是 ICurioItem

### 2026-07-10 — 游泳加速对标冲刺

- 改动：游泳加速公式从 `1.25 - 0.25 / desiredSpeedMult`（最大值渐进 1.25x）改为 `velocityMult = 1 + t × SWIM_MAX_MULTIPLIER × tierMult`（像冲刺的 MULTIPLY_TOTAL，最大值 9x）
- 原因：原公式无论 tierMult 多高、SWIM_MAX_MULTIPLIER 多大，速度至多 1.25 倍，实际等于加速没生效

### 2026-07-10 — 免疫系统：冲击/动能 + 灼烧

- 改动：新增 `IMMUNITY_IMPACT_KILLS`(30) / `IMMUNITY_FIRE_KILLS`(200) Config；`onLivingAttack` 中挡 `DamageTypes.FLY_INTO_WALL`（30 击杀解锁）和 `DamageTypeTags.IS_FIRE`（200 击杀解锁）；tooltip 新增"动能吸收"、"烈焰屏障"行
- 原因：为原型提供与击杀成长挂钩的防御能力

### 2026-07-10 — 下砸冻结彻底修复

- 改动：删除 `slamFreezeTick` 系统（triggerSlamImpact/onLivingTick/onPlayerTick START 三处清零 + NBT tag）；删除 `SLAM_ZERO_H`（加剧问题）；加 `player.hurtMarked = true` 强制同步服务端位置到客户端；CraterBlock.getCollisionShape 返回原始方块的碰撞箱（`orig.getCollisionShape(level, pos, ctx)`），而非 `Shapes.empty()`
- 根因追踪：CraterBlock 替换客户端地面方块为 `Shapes.empty()` → 客户端站不住往下掉 → 服务端有原始方块拉回 → 原地抽动。飞行能跑因为不依赖地面碰撞
- 原因：用户报告下砸后原地被困 40+ tick
- 关联：incidents/craft.20260710.slam-freeze-craterblock.md

### 2026-07-10 — 诊断日志系统

- 改动：新增 SLAM_DIAG（server LivingTick/END phase）、SLAM_DIAG_CLIENT（client tick，含输入检测）、SLAM_IMPACT（冲击瞬间）、movedXZ 实际位移追踪；SetSlamDiagTick 40 tick 窗口
- 原因：下砸冻结根因无法从代码分析定位，需要每 tick 速度/位置/输入追踪
- 教训：ServerPlayer.zza 永远为 0，服务端诊断靠服务器自行计算的 movedXZ 不可靠（不反映客户端位置更新），必须加客户端输入检测

