# Timeline: soul-dominion-focus

| Date | Event |
|------|-------|
| 2026-07-15 | 创建。grill-me 确定设计：全部伤害均分转移、Capability 非 Effect、NECROMANCY、只支持 DURATION/RANGE 附魔、120s 冷却、90s 基础持续、100 格范围。实施时参考了 StressShield 的 Capability 模式、ObsidianMonolithRenderer 的链渲染技术、王权 MANDATE 的 LivingAttackEvent 拦截模式。 |
| 2026-07-15 | 实施阶段：Capability + 网络同步 + Spell + Focus + Events + 渲染器完整实现。compileJava 通过。 |
| 2026-07-15 | 修复：onPlayerTick 从"检查存活"改为"全量重扫 IOwned"，新召唤仆从自动纳入链接。 |
| 2026-07-15 | 锁链渲染迭代：uniform 红→prisoner_chain 图案→链环纹理→方尖碑 8×8 链。最终方案：entityCutoutNoCull + 8×8 红链纹理 + 原始 UV（U=长度,V=宽度）。 |
| 2026-07-15 | **功能冻结归档** |
| 2026-07-15 | 删除 3 条状态栏提示（already_active / activated / all_servants_dead），静默返回。中英文 lang key 和对应代码一并清理。 |
| | 关联 incidents：`_craft/20260715.soul-dominion-chain-rendering.md` |
