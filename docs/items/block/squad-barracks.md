# 点将台 — goetypunk

## 身份

- 类型：block
- 注册类：ModBlocks.TIER1/2/3_SQUAD_BARRACKS
- Java 类：Tier1/2/3SquadBarracksBlock (extends SquadBarracksBlock)
- 方块实体：SquadBarracksBlockEntity
- 首次创建：2026-06-14

## 设计意图

三阶升级的多功能方块：编队配置 → 消耗胚胎 → 批量召唤。支持机械动力全方向输入、Chunk 常加载、跨维度操作。

## 演化记录

### 2026-06-15 — 初始设计与注册

- 改动：创建 Tier1/2/3SquadBarracksBlock + SquadBarracksBlockEntity + SquadBarracksMenu + SquadBarracksScreen。实现三阶升级、编队 GUI、机械动力兼容、Chunk Ticket、跨维度操作
- 原因：需要一个集中管理编队召唤的交互点，代替零散的右键单次召唤
- 关联：openspec squad-summon-system

### 2026-06-16 — Codebase Remediation: SquadChunkTickets 移动

- 改动：SquadChunkTickets 从 `common/block/entity/` 移至 `common/utils/`
- 原因：该类是工具类，不属于方块实体逻辑
- 关联：openspec codebase-remediation

### 2026-06-26 — 替换 armory platform 模型 + 拆分三阶独立纹理

- 改动：用桌面 armory_platform_pack 的 Blockbench 导出替换模型，装饰元素烘焙到纹理中移除 3D vein/rivet/panel 元素。tier2/tier3 各 5 张贴图放入独立子文件夹，三个阶各自独立的 blockstate/item/model（已改为指向独立文件而非共享）
- 原因：用户更新了资源包设计，三阶需要不同纹理
- 关联：用户指令替换
