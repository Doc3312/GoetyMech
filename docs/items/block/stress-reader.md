# 应力读取器 — GoetyMech

## 身份

- 类型：block
- 注册类：ModBlocks.STRESS_READER / ModItems.STRESS_READER
- Java 类：StressReaderBlock (extends DirectionalKineticBlock)
- 方块实体：StressReaderBlockEntity (extends KineticBlockEntity)
- 首次创建：2026-06-14

## 设计意图

Create 动力方块，读取所在网络的应力数据，与应力聚晶配合实现远程碾压轮攻击。

## 演化记录

### 2026-06-24 — 初始实现
- 2026-07-03 — 接收 Ark 远程消耗分配
  - `consumedRemotely` 字段 + setter，接收 Ark `distributeConsumption()` 分配的消耗值
  - `StressReaderBlock.use()` 增加 RemoteEngine 解绑分支：潜行+持绑定引擎物品 → 读取 NBT → 调 `clearBinding()` → 发消息
