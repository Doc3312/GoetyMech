## Context

当前 TimeFocusPouch 的时停触发逻辑挂在 `ScreenEvent.Opening` 上，当玩家打开 FocusPackScreen / FocusBagScreen / FocusRadialMenuScreen 时激活 `TimeFreezeHandler.frozen = true`，关闭屏幕时恢复。

经过三次迭代后发现的问题：

- **Iteration 1** (cancel `tickEntities()`): entity 冻结了但有视觉 jitter，因 `partialTick` 仍在递增导致渲染插值漂移
- **Iteration 2** (cancel `Entity.move()` + let `Entity.tick()` run): 火球旋转/怪物动画照常播放，因为 tick 方法在更新视觉状态
- **Iteration 3** (cancel `ClientLevel.tick()`): 过于激进，Goety 径向菜单卡死，而且实际测试发现 mixin 疑似未生效

参考 endinglibrary 的成熟方案：`ClientLevel.tick()` 全取消 + `Minecraft.getPartialTick()` 冻结 + 自定义主循环。但我们用 GUI 触发而非法术，不需要自定义主循环。

## Goals / Non-Goals

**Goals:**
- entity 位置不改变
- entity 视觉动画不播放（火球不旋转、怪物不动作）
- 粒子不更新
- 服务端 entity 不 tick（无 AI、无伤害）
- Boss 音乐正常播放
- Screen / GUI 正常交互（径向菜单不卡死）
- 渲染无 jitter（partialTick 冻结）
- 玩家在 GUI 打开期间视角固定

**Non-Goals:**
- 方块实体（熔炉、Create 轮毂）不冻结
- 时间/天气不冻结
- 多人模式支持（仅单机/LAN）
- 自定义主循环（不需要，GUI 触发比法术简单）

## Decisions

### Decision 1: 取消 `tickEntities()` 而非 `tick()`

参考 endinglibrary 的做法，但缩小冻结范围：

| 方案 | 效果 | 问题 |
|---|---|---|
| `cancel tick()` | 全冻结 | 径向菜单依赖 level 上下文，卡死 |
| `cancel tickEntities()` | entity 冻结，level 上下文正常 | 方块/时间继续运行 |
| `cancel tickEntities() + tickBlockEntities()` | entity + 方块冻结 | 可能仍影响菜单 |

**选择**: 只 `cancel tickEntities()`，因为 level 的 pending entity 处理和 chunk 刷新需要在 tick 中继续执行，径向菜单依赖它们。

### Decision 2: 冻结 `getPartialTick()` 返回 0

Minecraft 渲染时用 `getPartialTick()` 做双帧插值：

- Position: `xOld + (x - xOld) * partialTick`
- Rotation: `yRotO + (yRot - yRotO) * partialTick`
- Animation: `walkAnim.positionO + (walkAnim.position - walkAnim.positionO) * partialTick`

当 `tickEntities()` 被取消时，x == xOld、yRot == yRotO、position == positionO。如果 partialTick 正常递增，某些渲染计算（如 `tickCount + partialTick`）仍会产生视觉漂移。返回 0 后这些值全部恒定。

**选择**: 返回 0.0F，不返回 `timer.partialTick`，因为后者在 `tickEntities()` 取消后可能仍被 timer 更新。

### Decision 3: `MinecraftServerMixin` 用 `@WrapOperation` 包裹 `ServerLevel.tick()`

保持现有实现不变。`@WrapOperation` 在 `tickChildren()` 中拦截 `ServerLevel.tick()` 调用，冻结时跳过。`tickChildren()` 的其他处理（network、player list）继续执行。

### Decision 4: 保留 `ParticleEngineMixin`

即使 `tickEntities()` 取消不直接影响粒子引擎，保留单独的 cancel 作为冗余保证。

## Risks / Trade-offs

- **[Risk] Goety 径向菜单读取 entity 数据时可能依赖某些 tick 中的副作用**: 目前的分析表明只读操作（inventory items、curios）不依赖 tick。如果仍有问题，可降级为在 `tickEntities()` 中 selectively skip 而非全 cancel。
- **[Risk] `MinecraftMixin` 因 `disableRefMap=true` 被跳过**: `getPartialTick` 是 Forge 新增方法，不在 SRG 映射表中。`remap=false` 让 Mixin 直接用 MCP 名，在 dev 环境应正常。如果仍不生效，可改用 `@ModifyVariable` 或 `@Redirect`。
- **[Trade-off] BlockEntity 继续 tick**: 冻结期间熔炉继续烧、Create 轮毂继续转。这是冻结范围缩小的代价，对 boss 战影响可控（玩家在 GUI 中，无法与方块交互）。
- **[Trade-off] `getPartialTick=0` 影响相机**: GUI 打开期间鼠标输入被 Screen 拦截，玩家不会拖动视角。关闭 GUI 时冻结解除，partialTick 恢复正常。
