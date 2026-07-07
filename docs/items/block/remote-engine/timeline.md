# Remote Engine Timeline

- **2026-06-14** — 初始创建
- **2026-06-27** — 调试日志降级
- **2026-06-28** — 模型/纹理全面替换
- **2026-06-29** — 模型 v2 几何收窄
- **2026-07-03** — Ark 集成、消耗公式修复、解绑重构
- **2026-07-07** — 重构：删除 auto-pool/placerUUID/EngineStatus/tick/消耗追踪；纯 1:1 binding；事件驱动 push
- **2026-07-07** — 消费反馈：updateFromNetwork() 覆盖捕获本地 stress/capacity 并上报；工具提示增加「本地消耗」行和超应力红色标记；initialize() 冷启动主动读取网络数据
- **2026-07-07** — 工具提示修复：删除旧的 Capacity 行和 Source 行（显示错误值），仅保留 RPM + 本地消耗

## 待办

- Ark 自动分配 Engine（简化版 auto-pool）
