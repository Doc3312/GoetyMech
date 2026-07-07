# AbstractEphemeralFluid

**路径**: `common/fluid/frame/AbstractEphemeralFluid.java`
**角色**: 流体框架基类，继承 `FlowingFluid`，统一管理所有自定义流体的基础行为

## 职责

- 提供 `FlowingFluid` 必须的抽象方法默认实现
- 声明 `getDecayRate()` 抽象方法 — 消退速度（每 tick 下降几级）
- 声明 `getFluidBlock()` 抽象方法 — 关联的 LiquidBlock Supplier
- `canConvertToSource()` 强制返回 false（不可无限产生）
- 不可用于直接注册，必须被具体流体类（如 `SoulFluid`）继承

## 关键约定

- **`getFluidType()` 须为 public**：`IForgeFluid` 接口中为 `public`，覆写时不能降级
- **Source/Flowing 内部类写在具体流体中**，不在框架层：
  - Source: `getAmount()=8`, `isSource()=true`
  - Flowing: 覆写 `createFluidStateDefinition` 添加 `LEVEL` 属性
- **`createLegacyBlock`** 委托给 `getFluidBlock().get()`，返回 `EphemeralLiquidBlock` 实例

## 使用方式

```java
public class MyFluid extends AbstractEphemeralFluid {
    @Override public int getDecayRate() { return 1; }
    @Override protected Supplier<? extends LiquidBlock> getFluidBlock() { return ...; }
    @Override public Fluid getFlowing() { return ...; }
    @Override public Fluid getSource() { return ...; }
    @Override public FluidType getFluidType() { return ...; }
    @Override public Item getBucket() { return ...; }

    public static class Source extends MyFluid { ... }
    public static class Flowing extends MyFluid { ... }
}
```

## 变更记录

| 日期 | 改动 | 原因 |
|------|------|------|
| 2026-07-01 | 创建 | 短暂流体框架设计 |
| 2026-07-01 | 移除 `getTimerDuration()` / `scheduledTick()` 覆写 | 方法签名不匹配；逻辑移至 `EphemeralLiquidBlock.tick()` |
| 2026-07-01 | `getDecayRate()` 改为 `public` | 编译修正 |
