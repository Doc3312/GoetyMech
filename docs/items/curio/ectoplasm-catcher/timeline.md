# Ectoplasm Catcher Timeline

## 2026-07-09

- **重命名 + 机制重写**：`SoulCatcherCurio` → `EctoplasmCatcherCurio`，从纯击杀计数器改为灵魂拦截容器
- **移除**：旧击杀阈值机制（每5击杀→1灵质）
- **新增**：击杀生物拦截灵魂，每100魂→1灵质，优先级高于图腾/Arca
- **新增**：多饰品独立处理（按装备顺序逐个填充）
- **新增**：`ChangeSoulEnergyEvent.Gain` 拦截，防止灵魂同时进入原版系统
- **配置变更**：移除 `soul_catcher` 配置块，新增 `ectoplasm_catcher`（souls_per_ectoplasm=100, sound_cooldown=40）
- **工具提示修改**："灵质：0/100" → "聚魂：0/100"，英文 "Ectoplasm: 0/100" → "Soul: 0/100"
- **功能冻结归档**：工具提示文案定型，核心逻辑不再改动
  - 关联 openspec：`ectoplasm-catcher-redesign`
  - 关联 incidents：无

## 2026-07-13

- **2026-07-13** — 添加合成配方 (ectoplasm_catcher.json)
  - 原因：补全生存获取途径

## 2026-07-15

- **纹理替换**：`ectoplasm_catcher.png` 从 Desktop `soul_catcher_necklace.png` 覆盖