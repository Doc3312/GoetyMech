# RemoteEngineBlockEntity — GoetyMech

## 身份

- 类型：block-entity
- Java 类：RemoteEngineBlockEntity
- 关联方块：RemoteEngineBlock (`docs/items/block/remote-engine.md`)
- 首次创建：2026-06-14

## 设计意图

遥控引擎的方块实体，实现远程动力传输（GenerateKineticBlockEntity 扩展），
包含 RPM 管理、应力计算、网络链接逻辑。

## Timeline

- **2026-06-27** — 降级 `[TEST]` 调试日志从 INFO/WARN 为 DEBUG。
