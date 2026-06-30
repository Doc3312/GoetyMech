# EphemeralFluidConfig

**路径**: `common/fluid/frame/EphemeralFluidConfig.java`
**角色**: Builder 模式配置类，用于 `EphemeralLiquidBlock` 构造时传入粒子类型、接触效果、概率等参数

## 字段

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `particle` | `ParticleOptions` | null | 粒子类型（null = 无粒子） |
| `contactEffect` | `Consumer<Player>` | no-op | 接触效果回调 |
| `particleProbability` | float | 0.025f | 每 tick 生成粒子的概率 |

## 使用方式

```java
EphemeralFluidConfig.builder()
    .particle(ParticleTypes.SCULK_SOUL)
    .particleProbability(0.025f)
    .contactEffect(p -> SEHelper.increaseSouls(p, 50))
    .build();
```

## 变更记录

| 日期 | 改动 | 原因 |
|------|------|------|
| 2026-07-01 | 创建 | 流体框架设计 |
