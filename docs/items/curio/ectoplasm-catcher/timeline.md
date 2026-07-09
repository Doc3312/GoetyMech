# Ectoplasm Catcher Timeline

## 2026-07-09

- **重命名 + 机制重写**：`SoulCatcherCurio` → `EctoplasmCatcherCurio`，从纯击杀计数器改为灵魂拦截容器
- **移除**：旧击杀阈值机制（每5击杀→1灵质）
- **新增**：击杀生物拦截灵魂，每100魂→1灵质，优先级高于图腾/Arca
- **新增**：多饰品独立处理（按装备顺序逐个填充）
- **新增**：`ChangeSoulEnergyEvent.Gain` 拦截，防止灵魂同时进入原版系统
- **配置变更**：移除 `soul_catcher` 配置块，新增 `ectoplasm_catcher`（souls_per_ectoplasm=100, sound_cooldown=40）