## ADDED Requirements

### Requirement: Entity render position frozen during time freeze

When `TimeFreezeHandler.isFrozen()` is true, ALL entities SHALL render at the position they occupied at the moment freeze was activated. Changes to `Entity.x` / `Entity.y` / `Entity.z` / `Entity.xo` / `Entity.yo` / `Entity.zo` from network packets or other sources SHALL NOT affect the rendered position.

#### Scenario: Entity position frozen on freeze activate

- **WHEN** `TimeFreezeHandler.activate()` is called
- **THEN** each entity's current `position()` is captured into a snapshot map

#### Scenario: Render uses snapshot during freeze

- **WHEN** `Entity.getPosition(float)` is called during freeze
- **THEN** the return value SHALL be the captured snapshot position, NOT `lerp(xo, getX(), partialTick)`

#### Scenario: Entity position unfrozen on deactivate

- **WHEN** `TimeFreezeHandler.deactivate()` is called
- **THEN** the snapshot map SHALL be cleared, and `Entity.getPosition(float)` SHALL resume normal lerp behavior

#### Scenario: New entity spawned during freeze gets snapshot on first render

- **WHEN** an entity is added to the level while frozen
- **THEN** on its first render, its current `position()` SHALL be captured as its frozen snapshot

#### Scenario: Entity unloaded during freeze no longer tracked

- **WHEN** an entity is removed from the level while frozen
- **THEN** its snapshot entry SHALL be removed (or lazily ignored on null)

### Requirement: Player camera frozen during time freeze

When `TimeFreezeHandler.isFrozen()` is true, the player camera SHALL NOT jitter. Since the camera follows the player entity's render position, freezing the player entity's `getPosition(float)` SHALL also freeze the camera.

#### Scenario: Camera stable during freeze

- **WHEN** server sends `ClientboundPlayerPositionPacket` during freeze
- **THEN** player's render position SHALL remain at the snapshot position
- **THEN** camera SHALL NOT snap to the corrected position

### Requirement: Entity rotation frozen during time freeze

When `TimeFreezeHandler.isFrozen()` is true, entity yaw and pitch SHALL be visually frozen at the moment of freeze activation.

#### Scenario: Entity rotation frozen

- **WHEN** `TimeFreezeHandler.activate()` is called
- **THEN** each entity's current `yRot` and `xRot` are captured
- **WHEN** entity render rotation is computed during freeze
- **THEN** the captured rotation values SHALL be used instead of live `yRotO`/`xRotO`

### Requirement: Freeze state not persisted

The freeze snapshot map SHALL be ephemeral. On dimension switch, respawn, or world reload, the map SHALL be cleared and re-populated on the next freeze activation.

#### Scenario: Dimension switch clear

- **WHEN** player changes dimension during freeze
- **THEN** snapshot map SHALL be cleared and re-populated with entities in the new dimension
