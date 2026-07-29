# SoulFluid

**路径**: `common/fluid/soul/SoulFluid.java`
**角色**: 灵魂液流体类，继承 `AbstractEphemeralFluid`，是框架的第一个具体流体

## 属性

- **decayRate**: 2（每 tick 下降 2 级，即原版水的 2 倍速消退）
- **lightLevel**: 4（FluidType 中设定）
- **纹理**: 复用原版水纹理，tint 着色为 0xCC7CFCFF

## Source / Flowing

```java
public static class Source extends SoulFluid {
    getAmount() = 8, isSource() = true
}

public static class Flowing extends SoulFluid {
    createFluidStateDefinition 添加 LEVEL
    getAmount() = state.getValue(LEVEL), isSource() = false
}
```

## 注册

- 流体源: `soul_liquid`
- 流动变体: `soul_liquid_flowing`
- Block: `soul_liquid`（`EphemeralLiquidBlock`，timerDuration=100, decayRate=2）
- Bucket: `soul_liquid_bucket`
- FluidType: `soul_liquid`

## 变更记录

| 日期 | 改动 | 原因 |
|------|------|------|
| 2026-07-01 | 创建 | 短暂流体框架具体实现 |
| 2026-07-01 | `getTimerDuration()` 移除；`getDecayRate()` → public | 框架重构 |
| 2026-07-01 | 添加 `zh_cn.json` 中文译名"灵魂液" | 中文译名强制规则 |
