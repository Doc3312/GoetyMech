# EphemeralLiquidBlock

**路径**: `common/fluid/frame/EphemeralLiquidBlock.java`
**角色**: 可复用 LiquidBlock，封装了计时消失、快速消退、粒子生成、接触效果等通用行为

## 构造函数参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `fluid` | `Supplier<? extends FlowingFluid>` | 流体 Supplier |
| `properties` | `Properties` | 方块属性（须含 noCollission, randomTicks, noLootTable） |
| `config` | `EphemeralFluidConfig` | 粒子/接触效果配置 |
| `timerDuration` | int | 水源计时 tick（0 = 无计时） |
| `decayRate` | int | 消退速度（每 tick LEVEL +N，>7 则设空气） |

## 方法覆写约定

| 方法 | 约定 | 陷阱 |
|------|------|------|
| `onPlace` | **必须**先调度流体 spread tick，再调度自定义计时 tick | 只调度计时不调度 spread 会导致流体不流动 |
| `tick` | 签名是 `tick(BlockState, ServerLevel, BlockPos, RandomSource)` | 不是 `scheduledTick` |
| `neighborChanged` | **必须**重新调度 fluid spread tick | 缺少则邻居变化后流体卡住 |
| `entityInside` | 用 `level.getGameTime() % N` 做频率控制 | 禁止叠用 `player.tickCount % N`（二者偏移不同步） |
| `animateTick` | 客户端侧，水源方块按概率生成粒子 | 无 |

## tick() 行为

1. **水源（LEVEL=0）**：设空气，调度相邻流动块加速消退
2. **流动块无源**：`LEVEL += decayRate`，超界则设空气；自调度下一 tick
3. **流动块有源**：委托 `super.tick()` 走标准流体 spread 逻辑

## 变更记录

| 日期 | 改动 | 原因 |
|------|------|------|
| 2026-07-01 | 创建 | 流体框架设计 |
| 2026-07-01 | `scheduledTick` → `tick` | Mojang 映射下方法名不同 |
| 2026-07-01 | `onPlace` 增加 fluid spread tick 调度 | 原实现导致流体不流动 |
| 2026-07-01 | 新增 `neighborChanged` 覆写 | 邻居变化后流体卡住 |
