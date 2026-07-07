# 点将台 — GoetyMech

## 身份

- 类型：block
- 注册类：ModBlocks.TIER1/2/3_SQUAD_BARRACKS
- 方块类：Tier1/2/3SquadBarracksBlock (extends SquadBarracksBlock)
- 方块实体：SquadBarracksBlockEntity
- 首次创建：2026-06-14

## 设计意图

三阶升级的多功能方块：编队配置 → 消耗胚胎 → 批量召唤。支持机械动力全方向输入、Chunk 常加载、跨维度操作。

## Timeline

- **2026-06-15** — 创建 Tier1/2/3SquadBarracksBlock + BlockEntity + Menu + Screen
- **2026-06-16** — SquadChunkTickets 移至 common/utils/
- **2026-06-26** — 替换 armory platform 模型 + 拆分三阶独立纹理
- **2026-06-26~07-02** — 模型迭代修复（6 轮：对称化、UV 翻转、rotation 格式、贴图更新）
