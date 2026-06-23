# ClientWallRunState — goetypunk

## 身份

- 类型：misc (Client-side state)
- Java 类：ClientWallRunState
- 首次记录：2026-06-24

## 设计意图

纯客户端单例状态，存储爬墙检测结果（墙面法线方向、前后/侧移输入、冲刺状态、grace ticks），供 `MixinWallRunTravel` 在 `Player.travel()` 中读取以计算墙面运动向量。

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 首次文档记录 | 还清文档债务 | MixinWallRunTravel, PrototypeRenderEvents |
