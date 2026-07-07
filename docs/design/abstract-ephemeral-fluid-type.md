# AbstractEphemeralFluidType

**路径**: `common/fluid/frame/AbstractEphemeralFluidType.java`
**角色**: FluidType 基类，复用原版水的 still/flow 纹理，子类仅需覆写 `getFluidTintColor()` 返回 ARGB 值即可获得自定义颜色

## 职责

- 提供标准的 `FluidType.Properties` 默认值（viscosity=1000, density=1000, temperature=300, lightLevel=4）
- 提供 `IClientFluidTypeExtensions` 实现，映射到 `block/water_still` / `block/water_flow` 纹理
- 声明 `getFluidTintColor()` 抽象方法供子类施加颜色

## 使用方式

```java
public class MyFluidType extends AbstractEphemeralFluidType {
    private static final int TINT = 0xFFFF00FF;
    @Override protected int getFluidTintColor() { return TINT; }
}
```

## 变更记录

| 日期 | 改动 | 原因 |
|------|------|------|
| 2026-07-01 | 创建 | 流体框架设计 |
