# ModFluids

**路径**: `init/ModFluids.java`
**角色**: 所有流体的注册入口，管理 4 个 `DeferredRegister`（FLUID_TYPES / FLUIDS / BLOCKS / ITEMS）

## 注册架构

```java
FLUID_TYPES → FluidType (ForgeRegistries.Keys.FLUID_TYPES)
FLUIDS     → Fluid (ForgeRegistries.FLUIDS)
BLOCKS     → Block (ForgeRegistries.BLOCKS)
ITEMS      → Item (ForgeRegistries.ITEMS)
```

每个流体必须注册 6 个条目：FluidType × 1 + Fluid × 2（source/flowing）+ Block × 1 + Bucket × 1。

## 已注册流体

| 流体 | FluidType | Source | Flowing | Block | Bucket |
|------|-----------|--------|---------|-------|--------|
| soul_liquid | `soul_liquid` | `soul_liquid` | `soul_liquid_flowing` | `soul_liquid` | `soul_liquid_bucket` |

## 添加新流体的模板

参照 `SOUL_LIQUID_*` 的 6 个注册条目 + `EphemeralFluidConfig` 构造即可。

## 变更记录

| 日期 | 改动 | 原因 |
|------|------|------|
| 2026-07-01 | 创建 ModFluids + soul_liquid 注册 | 流体框架实现 |
| 2026-07-01 | `decayRate=2` 参数增加 | EphemeralLiquidBlock 构造签名变更 |
