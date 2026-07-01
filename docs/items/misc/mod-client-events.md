# Mod Client Events

客户端 Fabric/Forge 通用事件。

## Timeline
- **2026-06-24** — 初始实现。处理 key binding、client tick 等。
- **2026-07-02** — 注册 fleshCasketKey（Z 键），按 Z 打开手持血肉匣 GUI。
- **2026-07-02** — 为 3 个晶匣注册 `trigger_type` ItemProperty（A→0.0, B→0.5, C→1.0），使 GUI 图标随 NBT 切换纹理。
