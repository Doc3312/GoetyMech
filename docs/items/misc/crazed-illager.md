# Crazed Illager Entity

疯狂唤魔者实体类。Goety 仆从基类扩展，具备跳跃 + 冲刺 AI。

## Timeline
- **2026-06-24** — 初始实现。
- **2026-06-25** — 攻击机制改为 4 帧/次 × 5 次/秒，无视无敌帧，自伤 1 HP。
- **2026-06-29** — 完整编译通过，build 成功，runClient 启动验证通过。
- **2026-06-29** — 生成 Blockbench `.bbmodel` 文件（因路径错误 + Y-flip 脚本损坏 + PS 数组运算失败折腾多次）。
