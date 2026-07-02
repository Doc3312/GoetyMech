# 遥控引擎 — GoetyMech

## 身份

- 类型：block
- 注册类：ModBlocks.REMOTE_ENGINE / ModItems.REMOTE_ENGINE
- Java 类：RemoteEngineBlock (extends DirectionalKineticBlock)
- 方块实体：RemoteEngineBlockEntity
- 首次创建：2026-06-14

## 设计意图

Create 动力方块，可在无传统动力源的位置远程提供旋转动力。

## 演化记录

- 2026-06-28: 模型和纹理全面替换 — 使用桌面 engine_soul_pack 的新模型（Blockbench 导出，含 base_plate / main_body / frame / front_panel / top_knob 等 8 个 element）和 5 张新 PNG（engine_base / engine_body / engine_top / engine_dark / engine_panel）。旧模型 remote_engine_recessed 删除，blockstate 旋转逻辑随新模型前方面向（north）重新适配。
- 2026-06-29: 模型 v2 更新 — 所有 element 几何收窄（base_plate 从全宽 16→12，main_body 从全宽 16→10），移除 top_knob，display 旋转值同步更新
- 2026-07-03a — 修复消耗计算：`|cachedRpm| × ratio` 替代 `this.stress`，解决 HUD 应力为 0 / 应力上限异常 / Ark 分配归零。添加调试日志。
- 2026-07-03 — 自动绑定 Ark + 消耗分配架构
  - `BindingType` enum（NONE / ARK / READER）替代简单的绑定模式判断
  - `arkPos / arkDim / placerUUID` 字段，支持跨维度自动绑定 Ark
  - `tryAutoBindArk()` 在放置时检测玩家 `PLAYER_ACTIVE_ARK`，自动绑定到 Ark（fallback 到 READER 模式）
  - `refreshFromArk()` 从 Ark 读取 RPM/可用应力，并向 Ark 报告自己消耗
  - `calculateAddedStressCapacity()` 增加 `this.speed < 0.01f return 0` 检查（不转时不提供容量）
  - 工具提示：NONE 模式 `addToGoggleTooltip()` return false；ARK 模式显示 "Source: Ark总消耗 / Ark总可用"
  - Ark cached 数据（`cachedArkTotalAvailable / cachedArkEnginesConsumption`）写于 `!clientPacket` 外层，始终同步到 client
  - `setPlacedBy()` 始终存 `placerUUID`；`RemoteEngineBlockItem.useOn()` 潜行+绑定 Reader → `PASS` 交由 `StressReaderBlock.use()` 处理解绑
