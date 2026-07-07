# Stress Reader Timeline

- **2026-06-14** — 初始创建
- **2026-06-24** — 初始实现
- **2026-06-27** — 日志降级
- **2026-07-03** — Ark 集成、应力隔离、updateStressFor 恢复
- **2026-07-07** — 重构：删除 calculateStressApplied()/静态全局变量；只读模式；updateFromNetwork() 事件驱动推送
- **2026-07-07** — 消费反馈间接关联：StressLinkService 新增 getEngineConsumptionForOwner() 从 Reader 所有者追溯 Engine 消耗，Reader 本身无需变更
- **2026-07-07** — 消费反馈尝试：addToGoggleTooltip() 调 Service 查引擎消耗但客户端不显示（Service 仅限服务端）；需要改用 NBT 同步字段

## 待办

- 新增 remoteEngineConsumption 同步字段 + NBT 写入，工具提示/ comparator 扣减
