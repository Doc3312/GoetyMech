# 应力方舟 — GoetyMech

## 身份

- 类型：block
- 注册类：ModBlocks.STRESS_ARK / ModItems.STRESS_ARK
- 方块类：StressArkBlock (extends BaseEntityBlock)
- 方块实体：StressArkBlockEntity
- 首次创建：2026-06-14

## 设计意图

大容量应力存储/缓冲方块，存储 Create 网络中的过剩应力并可在需要时释放。HUD 显示容量/应力/网络连接数。

## Timeline

- **2026-06-14** — 初始创建
- **2026-06-24** — 初始实现完成
- **2026-07-03** — Ark 作为应力中枢重构
  - ArkLocation record（dimension + BlockPos）替代纯 BlockPos
  - boundEngines Map 跟踪绑定的引擎
  - register/unregister/reportConsumption() 接口
  - distributeConsumption() 按各 Reader 可用应力比例分配
  - getRepresentativeRPM() 返回可用应力最高的 Reader 的 RPM
  - PLAYER_ACTIVE_ARK 全局 Map
  - onArkRemoved() 清理
  - NBT 序列化
- **2026-07-03** — 应力分配公式修正：移除 `+ reader.getConsumedRemotely()`
- **2026-07-03** — 修复引擎注册持久性：reportConsumption() 自动注册、load() 中 lastSeenTick 设为当前游戏时间
- **2026-07-03** — 数据包增加引擎消耗：S2CStressArkDataPacket 新增 engineConsumption
