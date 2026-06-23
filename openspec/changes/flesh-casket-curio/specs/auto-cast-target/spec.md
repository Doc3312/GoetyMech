## ADDED Requirements

### Requirement: TargetingType 注册表

SHALL define `TargetingType` enum: `SELF`, `RETALIATORY`, `SUMMON`, `UTILITY`.
SHALL provide a `Map<Item, TargetingType>` initialized with Goety's known foci.

#### Scenario: TargetingType enum exists
- **WHEN** checking auto-cast target system
- **THEN** TargetingType.SELF, RETALIATORY, SUMMON, UTILITY SHALL be defined

### Requirement: SELF targeting

SELF foci SHALL target the player (caster).
Covered Goety foci: bulwark_focus, iron_hide_focus, soul_heal_focus, warding_focus, chilling_focus, flying_focus, launch_focus, leaping_focus, cushion_focus.

#### Scenario: Self-cast heal
- **WHEN** Type A triggers and slot 0 has SoulHealFocus (SELF type)
- **THEN** the spell SHALL cast on the player

### Requirement: RETALIATORY targeting

RETALIATORY foci SHALL target the attacker entity from the damage event.
If attacker is null (environment damage), SHALL fall back to player's look target (via `spell.getTarget(caster)` or `rayTrace`).
Covered Goety foci: magic_bolt_focus, soul_bolt_focus, sonic_boom_focus, fang_focus, spike_focus, lightning_focus, frost_nova_focus, fire_breath_focus, ice_spike_focus, etc.

#### Scenario: Retaliate against attacker
- **WHEN** skeleton shoots player, trigger fires, slot 0 has MagicBoltFocus (RETALIATORY)
- **THEN** auto-cast SHALL target the skeleton

### Requirement: SUMMON targeting

SUMMON foci SHALL target the player (foci spawn near the player).
Covered Goety foci: all necromancy summon foci, vexing_focus, frostborn_focus, blazing_focus, guardian_focus, etc.

#### Scenario: Summon minion near player
- **WHEN** auto-cast fires and slot 0 has SkeletonFocus (SUMMON)
- **THEN** skeletons SHALL spawn around the player

### Requirement: UTILITY targeting

UTILITY foci SHALL target the player.
Covered Goety foci: crafting_focus, ender_chest_focus, telekinesis_focus, recall_focus, blink_focus, soul_light_focus, glow_light_focus, etc.

#### Scenario: Open crafting
- **WHEN** auto-cast fires and slot 0 has CraftingFocus (UTILITY)
- **THEN** crafting GUI SHALL open

### Requirement: Unregistered focus fallback

If a focus is not registered in the targeting map, SHALL default to SELF.

#### Scenario: Unknown focus
- **WHEN** slot 0 contains an unregistered IFocus item
- **THEN** auto-cast SHALL default to SELF targeting (player)
