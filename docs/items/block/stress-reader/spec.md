# 应力读取器 — GoetyMech

## 身份

- 类型：block
- 注册类：ModBlocks.STRESS_READER / ModItems.STRESS_READER
- 方块类：StressReaderBlock (extends DirectionalKineticBlock)
- 方块实体：StressReaderBlockEntity (extends KineticBlockEntity)
- 首次创建：2026-06-14

## 设计意图

Create 动力方块，读取所在网络的应力数据，与应力聚晶配合实现远程碾压轮攻击。
方块实体读取 Create 动力网络应力数据。

## Timeline

- **2026-06-14** — 初始创建
- **2026-06-24** — 初始实现
- **2026-06-27** — 降级 `[TEST]` 调试日志从 INFO 为 DEBUG
- **2026-07-03** — 接收 Ark 远程消耗分配（consumedRemotely 字段 + setter）
- **2026-07-03** — 远程应力隔离：calculateStressApplied() 返回 0
- **2026-07-03** — 恢复 updateStressFor() 调用，触发 network recalc
