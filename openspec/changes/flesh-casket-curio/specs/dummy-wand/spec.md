## ADDED Requirements

### Requirement: AutoCastDummyWand 隐藏物品

SHALL register a hidden item `auto_cast_dummy_wand` that implements `IWand`.
SHALL NOT appear in any creative tabs, JEI, or recipe outputs.
SHALL have `stacksTo(1)` and no special properties.

#### Scenario: Item registered but hidden
- **WHEN** checking creative tab
- **THEN** auto_cast_dummy_wand SHALL NOT appear

### Requirement: Returns all SpellTypes

`getSpellTypes()` SHALL return all 11 values from `SpellType` enum (NONE, NECROMANCY, NETHER, ILL, FROST, GEOMANCY, WIND, STORM, ABYSS, WILD, VOID).
This ensures `Spell.rightStaff()` and `typeStaff()` always return true.

#### Scenario: rightStaff returns true
- **WHEN** ANY spell calls `rightStaff(autoCastDummyWandStack)`
- **THEN** it SHALL return true

### Requirement: Used for Advanced/Epic tiers

When auto-cast executes for tier 2 or 3 FleshCasket, SHALL create an `ItemStack` of `AutoCastDummyWand` and pass it as the `staff` parameter to `SpellResult()`.

#### Scenario: Advanced casts with dummy wand
- **WHEN** advanced FleshCasket auto-casts
- **THEN** `SpellResult(world, caster, dummyWandStack, stats)` SHALL be called
