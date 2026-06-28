# Crazed Illager Renderer

CrazedIllager 实体渲染器（MobRenderer + HierarchicalModel，非 GeckoLib）。

## Timeline
- **2026-06-24** — 初始实现（GeckoLib）。
- **2026-06-24** — 改为 HierarchicalModel（Java ModelPart），与 Goety 其他实体一致。
- **2026-06-25** — CrazedIllagerRenderer 使用 MobRenderer<Mob> 泛型以兼容 Hostile + Servant 两个 EntityType。
