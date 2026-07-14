# Stress Ark Timeline

- **2026-06-14** — 初始创建
- **2026-06-24** — 初始实现
- **2026-07-03** — 中枢重构、分配公式修正、引擎持久性修复、数据包增强
- **2026-07-07** — 重构：删除 getTotalSystemStress()；直接遍历 reader cache 累加
- **2026-07-07** — 消费反馈：refreshStats() 调用 service.getEngineConsumptionForOwner()；syncToClient 透传消耗值；HUD 显示净可用和引擎消耗行
- **2026-07-07** — 间接影响：StressLinkService readerToEngine 改 1:N，getEngineConsumptionForOwner() 聚合所有引擎消耗，Ark 消耗显示更准确
- **2026-07-08** — HUD 显示格式修正：文本从「剩余/容量」(netAvailable) 改为「已用/容量」(used)，与引擎 Goggle 显示格式一致；进度条逻辑不变
- **2026-07-08** — 存档加载修复：GlobalStressLinkManager 添加 loadTickDelay 延迟初始化机制；存档加载后等待10tick再填充 readerCache 和触发 distributeBudget()；修复重进存档后绑定关系丢失问题
- **2026-07-08** — HUD 持久化修复：添加 PlayerLoggedInEvent 监听；玩家登录时检查是否有活动 Ark，若无则发送清除包；修复破坏 Ark 后 HUD 不消失问题
- **2026-07-09** — 存档保存/加载深度修复：添加 shuttingDown 标志和 ServerStoppingEvent 监听；服务器关闭期间阻止 markDirty() 执行，防止第二次 save(File) 覆盖第一次保存的正确数据；添加 loading 标志，加载期间阻止 unbind() 删除绑定关系；修复重进存档后绑定关系丢失和 Ark 显示 0/0 的问题


## 2026-07-13

- **2026-07-13** — 添加合成配方 (stress_ark.json)
  - 原因：补全生存获取途径

## 2026-07-15

- **HUD 纹理替换 + 居中文字**：用 `engine_energy.png` 替换 Goety 的 `soul_energy.png`
  - 纹理来源：Desktop `engine_energy.png`（128×90 精灵表）
  - 背景条：Row 1 (v=9) 棕色背景
  - 绿色底条：Row 2 (v=21) 始终 117px 全满
  - 红色覆盖条：Row 3 (v=29) 按 `used/cap` 比例覆盖
  - 移除 Goety 的 `soul_energy_bar.png` 动画光效
  - 文字从固定偏移改为 `font.width()` 水平居中
  - 关联 incidents：`_meta.20260715.cross-session-overwrite.md`