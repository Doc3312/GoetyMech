# PrototypeRenderEvents — goetypunk

## 身份

- 类型：misc (Forge event subscriber, CLIENT)
- Java 类：PrototypeRenderEvents
- 首次记录：2026-06-24

## 设计意图

客户端侧的 Prototype 系统渲染与输入处理：

1. **Client tick** — 按键输入采集（WASD/冲刺/跳跃），墙面检测，C2S 发包（爬墙输入、蓄力跳跃、墙跳、取消 slam、飞行输入）
2. **Camera roll** — 爬墙时摄像机沿墙面法线旋转（quaternion SLERP）
3. **Model rotation** — 爬墙时玩家模型 body/head/arms/legs 匹配摄像机旋转
4. **Body render** — 在玩家身体上叠加 Prototype 纹理渲染
5. **Particles** — Slam 下坠时 body trail 粒子（DustParticle + Flame）

## 演化记录

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 首次文档记录 | 还清文档债务 | ClientWallRunState, PrototypeEvents, PrototypeItem |
