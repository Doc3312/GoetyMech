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
| 2026-06-24 | START 阶段加 shift 检测退出墙跑 | 用户需求：Shift 键松开墙面 | MixinWallRunTravel |
| 2026-06-24 | START 阶段用 shouldSprint() 守卫 sprintDown，sprinting 变量加 || ClientWallRunState.shouldSprint() | 翻越后 sprint 丢失，用 sprintRetainTicks 保持 30 tick | ClientWallRunState |
| 2026-06-24 | 删除 shouldSprint() 守卫，恢复直接按键轮询 sprintDown | shouldSprint() 在 grace 窗口期阻止 sprintDown 刷新 → 翻越后 sprint 断掉 | sprint-cutoff-fix incident |
| 2026-06-24 | setWallNormal 加 !player.isInWater() 守卫；camera roll 加 !player.isInWater() | 水下不应触发墙跑（移动+相机旋转） | PrototypeEvents |
| 2026-06-24 | 重构 charge jump C2S 发包逻辑：canCharge 下移到内层仅控制累加；外层改用 !isFallFlying；发包节流改为 tickCount%5；释放改为 !jumpDown | 跌落边缘时 onGround=false → canCharge=false → else 分支发 action=0 绕过服务端 3 格检测 | PrototypeEvents |
