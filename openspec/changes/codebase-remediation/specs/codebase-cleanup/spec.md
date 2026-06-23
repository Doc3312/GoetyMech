## ADDED Requirements

### Requirement: Remove dead renderer class
The system SHALL delete `client/entity/SculkSpikeRenderer.java` as it is unregistered dead code.

#### Scenario: Verify no references remain
- **WHEN** grepping for `SculkSpikeRenderer` in the codebase
- **THEN** only the file itself is returned, with no import or usage references

### Requirement: Remove empty event directory
The system SHALL delete the empty `com/goetypunk/addon/event/` directory.

#### Scenario: Verify directory is empty
- **WHEN** listing files in `com/goetypunk/addon/event/`
- **THEN** directory contains no `.java` files

### Requirement: Extract duplicate raytrace logic
The system SHALL extract the shared raytrace-to-find-spawn-position logic from `TwistedEmbryoItem` and `MatureEmbryoItem` into `MinionSpawnHelper.findSpawnPosition()`.

#### Scenario: TwistedEmbryoItem uses extracted method
- **WHEN** `TwistedEmbryoItem.use()` calls spawn
- **THEN** it uses `MinionSpawnHelper.findSpawnPosition()` instead of inline raytrace

#### Scenario: MatureEmbryoItem uses extracted method
- **WHEN** `MatureEmbryoItem.use()` calls spawn
- **THEN** it uses `MinionSpawnHelper.findSpawnPosition()` instead of inline raytrace

### Requirement: Move SquadChunkTickets to utils
The system SHALL move `SquadChunkTickets` from `common/block/entity/` to `common/utils/`.

#### Scenario: Import updated
- **WHEN** `SquadBarracksBlockEntity` imports `SquadChunkTickets`
- **THEN** it imports from `com.goetypunk.addon.common.utils.SquadChunkTickets`

### Requirement: Extract network channel to dedicated class
The system SHALL extract `SimpleChannel` definition from `GoetyPunkMod.java` into a new `ModNetwork.java` in `common/net/`.

#### Scenario: GoetyPunkMod references ModNetwork
- **WHEN** `GoetyPunkMod.NETWORK` is accessed
- **THEN** it points to `ModNetwork.CHANNEL`
