# 时停聚晶袋 — goetypunk

## 身份

- 类型：pouch
- 注册类：ModItems.TIME_FOCUS_POUCH
- Java 类：TimeFocusPouch (extends FocusPack)
- 首次创建：2026-06-14

## 设计意图

打开 GUI 时触发时空冻结。客户端 entity 位置/动画/粒子冻结，渲染 partialTick 归零消除 jitter，服务端维度 tick 暂停。

## 演化记录

### 2026-06-18 — Iteration 1：取消 tickEntities() 而非整条 tick()

- 改动：ClientLevelMixin 注入目标从 tick() 改为 tickEntities()
- 原因：取消 tick() 导致 Goety 径向菜单卡死（依赖 level 上下文）
- 关联：openspec time-freeze-jitter-fix

### 2026-06-18 — 新增 MinecraftMixin 冻结 partialTick

- 改动：getPartialTick() 在时停中返回 0.0F
- 原因：entity 位置冻结后，partialTick 继续递增导致渲染插值漂移（jitter）
- 关联：openspec time-freeze-jitter-fix

### 2026-06-19 — Iteration 3：Entity.getPosition(float) 快照覆盖

- 改动：创建 EntityMixin 快照系统，冻结 activate 时捕获所有 entity 位置/旋转
- 原因：服务端持续发送位置包 → setPos() 更新 xo/x → getPosition(0)=xo → camera 每包跳跃
- 关联：openspec fix-freeze-jitter

### 2026-06-19 — 快照生命周期完善

- 改动：支持新 entity 生成时懒加载、移除 entity 时清理、deactivate 清空 map
- 原因：冻结期间新增/卸载的 entity 需要正确的快照行为
- 关联：openspec fix-freeze-jitter
