# Stress Reader Timeline

- **2026-06-14** — 初始创建
- **2026-06-24** — 初始实现
- **2026-06-27** — 日志降级
- **2026-07-03** — Ark 集成、应力隔离、updateStressFor 恢复
- **2026-07-07** — 重构：删除 calculateStressApplied()/静态全局变量；只读模式；updateFromNetwork() 事件驱动推送
- **2026-07-07** — 消费反馈间接关联：StressLinkService 新增 getEngineConsumptionForOwner() 从 Reader 所有者追溯 Engine 消耗，Reader 本身无需变更
- **2026-07-07** — 消耗反馈 + 1:N 绑定：新增 remoteEngineConsumption 字段+NBT；覆盖 getNetworkStress() 包含引擎消耗；comparator 简化；tooltip 移除重复行；setRemoteEngineConsumption 通过 sendData 同步客户端
- **2026-07-08** — 将远程引擎消耗从显示注入迁移为 Create 真实应力结算：恢复 `calculateStressApplied()` 动态基数计算，`setRemoteEngineConsumption()` 改为调用 `updateStressFor()` 触发网络重算，并删除 `updateFromNetwork()` / `setRemoteEngineConsumption()` 中手工伪造总应力的覆盖逻辑
