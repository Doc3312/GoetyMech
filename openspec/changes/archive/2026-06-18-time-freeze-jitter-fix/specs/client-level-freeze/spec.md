## ADDED Requirements

### Requirement: Client entity ticking frozen during time freeze

When `TimeFreezeHandler.isFrozen()` returns true, `ClientLevel.tickEntities()` MUST be cancelled before any entity ticking occurs. This prevents all client-side entity position changes, AI updates, and animation state updates.

#### Scenario: Entity position frozen
- **WHEN** `TimeFreezeHandler.isFrozen()` is true AND `ClientLevel.tickEntities()` is called
- **THEN** the method SHALL return immediately without ticking any entities

#### Scenario: Entity animation frozen
- **WHEN** time freeze is active AND an entity's `tick()` would normally run
- **THEN** the entity's `tickCount` SHALL NOT increment, its rotation SHALL NOT update, and its walk animation SHALL NOT advance

### Requirement: Non-entity level processing preserved

`ClientLevel.tick()` as a whole MUST NOT be cancelled. Only `tickEntities()` SHALL be intercepted. This preserves block entity ticking, time updates, weather, and pending entity processing that Goety's radial menu depends on.

#### Scenario: Block entities continue ticking
- **WHEN** time freeze is active
- **THEN** `tickBlockEntities()` SHALL still execute normally
- **AND** block entities like furnaces SHALL continue their smelting progress

#### Scenario: Goety radial menu functional
- **WHEN** the player opens FocusRadialMenuScreen during time freeze
- **THEN** the screen SHALL render and respond to input without hanging or crashing
- **AND** the player's focus items SHALL be readable from inventory
