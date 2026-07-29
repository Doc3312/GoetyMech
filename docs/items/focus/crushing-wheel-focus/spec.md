# Crushing Wheel Focus — Spec

## Registry

| Field | Value |
|-------|-------|
| Registry Name | `goetymech:crushing_wheel_focus` |
| Class | `CrushingWheelFocus` (extends `MagicFocus`) |
| Item Tab | `ModCreativeTab` |

## Behavior

- 右键绑定 Stress Reader，Shift+右键解绑
- 通过 CrushingWheelSpell 施法，使用 Wand 召唤一排粉碎轮向前滚动碾压敌人
- 绑定的 Reader 网络容量越大、伤害越高（公式：capacity / divisor）
- 如果有 Stress Ark，优先读取聚合容量
- 当 used > cap 时 tooltip 显示红色过载

## Config

| Key | Default | Range | Notes |
|-----|---------|-------|-------|
| `crushing_wheel_focus.soul_cost` | 0 | 0-1000 | 灵魂消耗 |
| `crushing_wheel_focus.cooldown` | 400 (20s) | 5-1200 | 冷却 ticks |
| `crushing_wheel.damage_divisor` | 2000 | 100-10000 | 应力→伤害除数（共享） |

## Spell: CrushingWheelSpell

- Soul Cost: `ModConfig.CRUSHING_WHEEL_FOCUS_COST`
- Cooldown: `ModConfig.CRUSHING_WHEEL_FOCUS_COOLDOWN`
- Cast Duration: 20 ticks
- Spell Type: `VOID`
- 召唤 1 + PotencyLevel 个 CrushingWheelEntity（最多 9 个）
- 法杖增幅伤害 ×1.5
- 碾压轮按玩家视线方向飞行，带有距离上限

## Rendering

- Item model: `item/generated` with texture `goetymech:item/crushing_wheel_focus`
- HUD tooltip: 显示当前应力伤害值（从 Ark 或 bound Reader 读取）

## Crafting

- 合成配方：`data/goetymech/recipes/crushing_wheel_focus.json`
