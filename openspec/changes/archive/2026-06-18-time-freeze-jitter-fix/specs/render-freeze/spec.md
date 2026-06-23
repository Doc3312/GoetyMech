## ADDED Requirements

### Requirement: Render partial tick frozen during time freeze

When `TimeFreezeHandler.isFrozen()` returns true, `Minecraft.getPartialTick()` MUST return 0.0F. This prevents the render interpolation system from producing visual drift when entity state is frozen.

#### Scenario: Entity position render frozen
- **WHEN** `TimeFreezeHandler.isFrozen()` is true
- **THEN** `Minecraft.getPartialTick()` SHALL return 0.0F
- **AND** all entity rendering SHALL use the last ticked position without interpolation

#### Scenario: Rotation render frozen
- **WHEN** time freeze is active AND an entity with rotation is rendered
- **THEN** the entity SHALL render at its last ticked rotation (`yRotO`, `xRotO`)
- **AND** rotating entities like fireballs SHALL appear stationary

#### Scenario: No jitter from stale interpolation
- **WHEN** time freeze is active AND entities are rendered
- **THEN** there SHALL be no visible jitter, stutter, or positional drift
- **AND** entities SHALL appear completely frozen in place

### Requirement: Camera rotation stable

During time freeze, the player's camera rotation SHALL remain fixed. Mouse look input is captured by the open Screen (FocusPackScreen / FocusRadialMenuScreen) and SHALL NOT rotate the camera.

#### Scenario: Camera fixed while screen open
- **WHEN** time freeze is active AND a Screen is open
- **THEN** the camera rotation SHALL NOT change in response to mouse movement
