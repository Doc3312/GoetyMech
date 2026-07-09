# Ectoplasm Catcher（灵质捕手）

## 注册信息

- 注册名：`goetymech:ectoplasm_catcher`
- 物品类：`EctoplasmCatcherCurio` extends `Item` implements `ICurioItem`
- 事件类：`EctoplasmCatcherEvents`
- Curios 槽位：charm（护符槽）、hands（手部槽）

## 机制

### 灵魂拦截

1. 玩家（或仆从）击杀生物时，以 `EventPriority.HIGHEST` 在 `LivingDeathEvent` 中先于 Goety 处理
2. 调用 `SEHelper.getSoulGiven(victim)` 计算生物提供的灵魂值
3. 灵魂值存入饰品 NBT `ectoplasm_souls`
4. 每 100 灵魂自动转化为 1 个灵质（`ModItems.ECTOPLASM`），溢出量继续保留

### 多饰品支持

按 Curios 槽位顺序逐个填充：第一个填满触发转化后，剩余灵魂给第二个，以此类推。

### 灵魂拦截阻断

同样以 `EventPriority.HIGHEST` 监听 `ChangeSoulEnergyEvent.Gain`，捕获到玩家有灵质捕手吸收标记时取消事件，使 Goety 原版的图腾/Arca 不会重复获得灵魂。

## 配置项

| 配置键 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `ectoplasm_catcher.souls_per_ectoplasm` | int | 100 | 每灵质所需灵魂数 |
| `ectoplasm_catcher.sound_cooldown_ticks` | int | 40 | 转化音效冷却（tick） |