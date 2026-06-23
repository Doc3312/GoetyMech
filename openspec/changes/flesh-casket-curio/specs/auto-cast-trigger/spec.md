## ADDED Requirements

### Requirement: Type A — 受击反击

When any living entity damages the player, if the player has Type A FleshCasket equipped in BACK slot, SHALL attempt to auto-cast the first available focus (left to right, off-cooldown, sufficient souls).

#### Scenario: Player takes damage
- **WHEN** player with Type A FleshCasket is damaged by a zombie
- **THEN** system SHALL find the first off-cooldown focus in the casket
- **THEN** IF focus has a RETALIATORY target type, target SHALL be the zombie
- **THEN** SHALL execute the spell with halved soul cost
- **THEN** SHALL set independent cooldown for that focus

#### Scenario: Type A retaliatory targeting for environment damage
- **WHEN** player with Type A FleshCasket takes fall damage (no attacker entity)
- **THEN** system SHALL fall back to player's look target
- **THEN** SHALL execute the spell targeting the entity under crosshair

### Requirement: Type B — 半血触发

When player's health drops to 50% or below, Type B FleshCasket SHALL trigger. SHALL check on `LivingHurtEvent`. SHALL trigger once per health threshold crossing (prevent spam).

#### Scenario: Health drops to exactly 50%
- **WHEN** player with Type B FleshCasket (full health 20) takes 10 damage, reaching 10 HP (50%)
- **THEN** SHALL trigger auto-cast once
- **THEN** SHALL NOT re-trigger until health rises above 50% and drops again

### Requirement: Type C — 可配置阈值

Same as Type B, but threshold is configurable via ModConfig (default 20%).
Config key: `FLESH_CASKET_THRESHOLD` (int, 1-100, default 20).

#### Scenario: Default threshold
- **WHEN** player with Type C FleshCasket has default config
- **THEN** SHALL trigger when health ≤ 20% of max health

#### Scenario: Custom threshold
- **WHEN** config `FLESH_CASKET_THRESHOLD` is set to 35
- **THEN** SHALL trigger when health ≤ 35% of max health

### Requirement: Minimum cast interval

All types SHALL enforce a minimum 5-tick interval between auto-casts to prevent event loops.

#### Scenario: Rapid damage
- **WHEN** player takes multiple damage events within 5 ticks
- **THEN** only the first event SHALL trigger auto-cast
- **THEN** subsequent events within 5 ticks SHALL be ignored

### Requirement: 从左到右遍历

When trigger fires, SHALL iterate internal slots from index 0 to N-1, selecting the first slot meeting all conditions (has IFocus item, focus off cooldown, player has enough souls for half cost, player alive, spell conditionsMet).

#### Scenario: First available focus is used
- **WHEN** slot 0 has a focus on cooldown, slot 1 has a ready focus
- **THEN** SHALL cast slot 1's focus
