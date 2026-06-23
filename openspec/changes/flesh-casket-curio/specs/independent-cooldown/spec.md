## ADDED Requirements

### Requirement: NBT-based cooldown storage

FleshCasketItem SHALL store cooldown data in its own NBT as a compound tag mapping focus item registry names to remaining cooldown ticks.
Format: `{cooldowns: {"goety:soul_bolt_focus": 100, "goety:bulwark_focus": 60}}`

#### Scenario: Cooldown stored
- **WHEN** auto-cast executes soul_bolt_focus
- **THEN** `goety:soul_bolt_focus` SHALL be stored in cooldowns NBT with spell's cooldown value

### Requirement: Per-focus cooldown tracking

Each focus item SHALL have its own independent cooldown within the same FleshCasket.
Cooldown value SHALL use `spell.spellCooldown(caster)` (includes attribute discounts).

#### Scenario: Multiple foci different cooldowns
- **WHEN** slot 0 has focus A (cooldown 100), slot 1 has focus B (cooldown 200)
- **THEN** focus A SHALL be ready after 100 ticks, focus B after 200 ticks, independently

### Requirement: PlayerTickEvent tick-down

`PlayerTickEvent` SHALL iterate all FleshCaskets in player's Curios BACK slot.
For each casket, decrement all cooldown entries by 1 per tick.
Remove entries reaching ≤ 0.

#### Scenario: Cooldown ticking
- **WHEN** focus cooldown is 100 ticks
- **THEN** after 50 ticks, remaining cooldown SHALL be 50
- **THEN** after 100 total ticks, the entry SHALL be removed (focus ready)

### Requirement: Independent from SEHelper cooldown

Cooldowns SHALL NOT call `SEHelper.addCooldown()` or `SEHelper.isOnCooldown()`.
SHALL be completely decoupled from player's Goety spell cooldown system.

#### Scenario: Separate cooldown systems
- **WHEN** player manually casts SoulBoltFocus via wand (sets SEHelper cooldown)
- **AND** FleshCasket auto-casts SoulBoltFocus
- **THEN** the two cooldowns SHALL NOT interfere with each other
