# Crazed Illager Entity

疯狂唤魔者实体类。Goety 仆从基类扩展，具备跳跃 + 冲刺 AI。

## Timeline
- **2026-06-24** — 初始实现。
- **2026-06-25** — 攻击机制改为 4 帧/次 × 5 次/秒，无视无敌帧，自伤 1 HP。
- **2026-06-29** — 完整编译通过，build 成功，runClient 启动验证通过。
- **2026-06-29** — 生成 Blockbench `.bbmodel` 文件（因路径错误 + Y-flip 脚本损坏 + PS 数组运算失败折腾多次）。
- **2026-07-01** — doHurtTarget 重写：速度恢复消除 hurt() 内置 0.4 固定击退；简化最小伤害保底逻辑；applyRaidBuffs 清空武器（设计上空手用头部锯刃攻击）。
- **2026-07-01** — 确认设计：空手攻击、自伤 1 HP 黏住玩家（KNOCKBACK_RESISTANCE=1.0 防止自身被击退）。
