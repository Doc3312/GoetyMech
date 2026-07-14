# Remote Engine Timeline

- **2026-06-14** — 初始创建
- **2026-06-27** — 调试日志降级
- **2026-06-28** — 模型/纹理全面替换
- **2026-06-29** — 模型 v2 几何收窄
- **2026-07-03** — Ark 集成、消耗公式修复、解绑重构
- **2026-07-07** — 重构：删除 auto-pool/placerUUID/EngineStatus/tick/消耗追踪；纯 1:1 binding；事件驱动 push
- **2026-07-07** — 消费反馈：updateFromNetwork() 覆盖捕获本地 stress/capacity 并上报；工具提示增加「本地消耗」行和超应力红色标记；initialize() 冷启动主动读取网络数据
- **2026-07-07** — 自动绑定 Ark：新增 placerUUID 字段+NBT；setPlacedBy() 存储；initialize() 调用 tryAutoBindArk()；tryAutoBindArk 通过 service.getReadersForOwner 找同玩家 Reader
- **2026-07-08** — StackOverflow 死循环修复：pushToEngine 添加 suppressEngineUpdates 重入防护，阻断 applyRemoteUpdate → engine 网络变化 → onEngineNetworkChanged → setRemoteEngineConsumption → reader 网络 sync → pushToEngine 的递归循环；budget 公式修正加回当前引擎消耗避免双重计入
- **2026-07-08** — Goggle 显示改进：「本地消耗」改名为「已分配应力」；新增「网络应力」行显示 reader 网络总使用/总容量；applyRemoteUpdate 增加 readerStress 参数，pushToEngine 传递 reader.getNetworkStress()；NBT 新增 RemoteReaderStress 字段
- **2026-07-08** — overstrained 隔离 + CME 修复：StressLinkService 改为延迟推送（dirtyReaders + ServerTickEvent）避免遍历期间修改集合导致 ConcurrentModificationException；按引擎加入顺序（LinkedHashSet）批量分配预算，新引擎应力不足时标记为过载（budget=0）且其消耗不计入 reader 网络，保护已有引擎正常运转
- **2026-07-09** — 存档保存/加载深度修复：添加 shuttingDown 标志和 ServerStoppingEvent 监听；服务器关闭期间阻止 markDirty() 执行，防止第二次 save(File) 覆盖第一次保存的正确数据；添加 loading 标志，加载期间阻止 unbind() 删除绑定关系；修复重进存档后绑定关系丢失和 Ark 显示 0/0 的问题
- **2026-07-09** — 修复 tryAutoBindArk 缺少 Ark 存在性检查：方法名暗示"有 Ark 时才自动绑定"，但实现只检查了是否有 Reader；新增 `getArkCountForOwner() > 0` 守卫条件，无 Ark 时跳过自动绑定（需手工通过物品右击 Reader 绑定）
- **2026-07-13** — 碰撞箱 + 遮挡修复
  - 碰撞箱从 `AllShapes.CASING_12PX` 改为 `VoxelShaper.forDirectional(box(2,0,0,14,14,16), NORTH)` 匹配模型实际范围
  - 新增 `getOcclusionShape` 返回 `Shapes.empty()` 修复背后方块连接面材质丢失
  - 关联 incidents：`_craft.20260713.occlusion-shape-pattern.md`


## 2026-07-13

- **2026-07-13** — 添加合成配方 (remote_engine.json)
  - 原因：补全生存获取途径