# 应力方舟 — GoetyMech

## 身份

- 类型：block
- 注册类：ModBlocks.STRESS_ARK / ModItems.STRESS_ARK
- Java 类：StressArkBlock (extends BaseEntityBlock)
- 方块实体：StressArkBlockEntity
- 首次创建：2026-06-14

## 设计意图

大容量应力存储/缓冲方块，存储 Create 网络中的过剩应力并可在需要时释放。HUD 显示容量/应力/网络连接数。

## 演化记录

### 2026-07-03 — Ark 作为应力中枢重构
- `ArkLocation` record（dimension + BlockPos）替代纯 BlockPos，支持跨维度查找
- `boundEngines` Map（`BlockPos → RemoteEngineBlockEntity`）跟踪绑定的引擎
- `register/unregister/reportConsumption()` 接口供 RemoteEngine 调用
- `distributeConsumption()` 将引擎总消耗按各 Reader 可用应力比例分配
- `getRepresentativeRPM()` 返回可用应力最高的 Reader 的 RPM
- `PLAYER_ACTIVE_ARK` 全局 Map（`UUID → ArkLocation`）存玩家最后放置/交互的 Ark
- `onArkRemoved()` 清理所有绑定的引擎
- NBT 序列化：ArkLocation（dim/pos）保存/读取
- **2026-07-03** — 应力分配公式修正：`distributeConsumption()` 移除 `+ reader.getConsumedRemotely()`，可用应力 = `capacity - stress`（真实本地方块应力）。
