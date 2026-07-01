# Crazed Illager Servant

疯狂唤魔者仆从版本。

## Timeline
- **2026-06-24** — 初始实现。
- **2026-07-01** — doHurtTarget 重写：改用 ModDamageSource.summonAttack 修复杀戮归属（击杀归召唤者）；速度恢复消除 hurt() 内置 0.4 固定击退；简化最小伤害保底逻辑；populateDefaultEquipmentSlots 清空武器（设计上空手攻击）。
