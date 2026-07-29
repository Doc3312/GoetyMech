# stress-shield Timeline

- **2026-06-24** — 初始实现。
- **2026-07-02** — 重构为完整 Capability 方案：IStressShield + StressShieldProvider + StressShieldHelper；新增被动恢复（5s 无伤后每秒恢复 20%）；新增 StressShieldFocus 聚晶物品（独立绑定 NBT）；新增 StressReaderBlock 绑定支持；注册到创造栏。
- **2026-07-09** — Ark 优先读取 + 修正为 capacity
  - 改动：`readStress` 改为 Ark 优先 → bound 回退；数据源从 `stress` 修正为 `capacity`；tooltip 从客户端 Ark HUD 动态计算护盾值
  - 原因：与应力聚晶统一行为，有 Ark 时无需绑定
  - 关联：StressShieldSpell, StressShieldFocus, StressShieldHelper

- **2026-07-13** — 添加合成配方 (stress_shield_focus.json)
  - 原因：补全生存获取途径

## 2026-07-13 — 渲染修复 + 音效 + 碰撞/优先级/冷却大修

- **渲染修复**:
  - RenderType 改为每帧动态创建（UV 波浪流动）
  - 新增 DepthTest ALWAYS（护盾不被盔甲遮挡）
  - 身体颜色改为淡蓝 `(0.6, 0.85, 1.0)` 区分环绕碎片
  - CubeDeformation 0.5 → 0.65 消除模型间隙
- **音效**: 复用 Goety `ModSounds`（SHIELD_DEBRIS_IMPACT / WILDFIRE_SHIELD_BREAK / SHIELD_UP）
- **Bug 修复**:
  - 消除双重注册（GoetyMechMod 中移除手动 register）
  - 冷却守卫从 `gameTime <= lastHurtTick` 改为 `gameTime - lastHurtTick < 5`（5 tick 内置冷却）
  - SKIP 分支加 `event.setCanceled(true)`（消除冷却期间的受击效果）
  - onLivingHurt 加 bypass 检查（修复 `/kill` 被 FALLBACK 拦截）
- **优先级交换**: `onLivingAttack` 从 HIGHEST 降为 NORMAL（PrototypeEvents 升至 HIGHEST 先跑小伤害免疫）
- **移除 hurtTime 方案**: `player.hurtTime` 在 Player 实体上不可靠，改用盾内置冷却
- **关联**: `StressShieldEvents.java`, `StressShieldLayer.java`, `StressShieldWorldRenderer.java`, `StressShieldOverlay.java`, `GoetyMechMod.java`

## 2026-07-15

- **纹理 + 本地冷却**：
  - 纹理：`stress_shield_focus.png` 从 Desktop `create_gear_shield.png` 重新覆盖
  - 本地配置（run/config/goetymech-common.toml）`stress_shield_focus.cooldown` 改为 200t (10s)（源码默认 12000t/10min 不变）