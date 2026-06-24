# AssimilationSaveManager — goetypunk

## 身份

- 类型：data (文件缓存工具)
- Java 类：AssimilationSaveManager
- 首次创建：2026-06-25

## 设计意图

为同化击杀计数提供独立的文件级持久化，防止游戏崩溃导致 PersistentData 丢失击杀数。

### 工作机制

1. **markDirty(ServerPlayer)** — 击杀后标记脏
2. **每 1200 tick（≈60s）** — TickEvent.ServerTickEvent 触发 flushDirty，遍历脏玩家写盘
3. **写盘策略** — 先写 .tmp 文件，再 AtomicMove 重命名，防止写一半崩溃截断
4. **登录恢复** — onPlayerLogin 比较缓存 vs NBT，cache > NBT 则恢复
5. **跳过优化** — 无脏玩家时 flush 直接返回，不产生 I/O

### 文件位置

`<世界>/playerdata/<UUID>_assimilation.dat`（与玩家 NBT 同级）

## 演化记录

### 2026-06-25 — 创建

- 响应 crash 导致击杀数清零的教训
- 关联：PrototypeEvents (LivingDeath markDirty + login recovery + ServerTick flush)
