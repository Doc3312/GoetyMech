# Stress Reader Timeline

- **2026-06-14** — 初始创建
- **2026-06-24** — 初始实现
- **2026-06-27** — 日志降级
- **2026-07-03** — Ark 集成、应力隔离、updateStressFor 恢复
- **2026-07-07** — 重构：删除 calculateStressApplied()/静态全局变量；只读模式；updateFromNetwork() 事件驱动推送
- **2026-07-07** — 消费反馈间接关联：StressLinkService 新增 getEngineConsumptionForOwner() 从 Reader 所有者追溯 Engine 消耗，Reader 本身无需变更
- **2026-07-07** — 消耗反馈 + 1:N 绑定：新增 remoteEngineConsumption 字段+NBT；覆盖 getNetworkStress() 包含引擎消耗；comparator 简化；tooltip 移除重复行；setRemoteEngineConsumption 通过 sendData 同步客户端
- **2026-07-08** — 将远程引擎消耗从显示注入迁移为 Create 真实应力结算：恢复 `calculateStressApplied()` 动态基数计算，`setRemoteEngineConsumption()` 改为调用 `updateStressFor()` 触发网络重算，并删除 `updateFromNetwork()` / `setRemoteEngineConsumption()` 中手工伪造总应力的覆盖逻辑
- **2026-07-08** — StackOverflow 死循环修复：StressLinkService.pushToEngine 添加 suppressEngineUpdates 重入防护；budget 公式修正为 `capacity - reader.getNetworkStress() + thisEngineConsumption` 避免双重计入；StressArkHUD drawStressBar 移除 engineConsumption 重复扣减
- **2026-07-08** — overstrained 隔离 + CME 修复：StressLinkService 改为延迟推送（dirtyReaders + ServerTickEvent）避免遍历期间修改集合导致 ConcurrentModificationException；按引擎加入顺序（LinkedHashSet）批量分配预算，新引擎应力不足时标记为过载（budget=0）且其消耗不计入 reader 网络，保护已有引擎正常运转
- **2026-07-09** — 存档保存/加载深度修复：添加 shuttingDown 标志和 ServerStoppingEvent 监听；服务器关闭期间阻止 markDirty() 执行，防止第二次 save(File) 覆盖第一次保存的正确数据；添加 loading 标志，加载期间阻止 unbind() 删除绑定关系；修复重进存档后绑定关系丢失和 Ark 显示 0/0 的问题
