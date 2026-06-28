# 遥控引擎 — goetypunk

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
