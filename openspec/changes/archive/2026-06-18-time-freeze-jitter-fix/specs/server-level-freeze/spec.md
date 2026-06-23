## ADDED Requirements

### Requirement: Server level ticking frozen during time freeze

When `TimeFreezeHandler.isFrozen()` returns true, `ServerLevel.tick()` SHALL be skipped inside `MinecraftServer.tickChildren()`. This prevents server-side entity AI, damage processing, and block ticking.

#### Scenario: Server entity AI frozen
- **WHEN** time freeze is active AND `MinecraftServer.tickChildren()` is executing
- **THEN** `ServerLevel.tick()` SHALL NOT be called for any dimension
- **AND** server-side entity AI SHALL NOT run

#### Scenario: Server networking remains active
- **WHEN** time freeze is active
- **THEN** `MinecraftServer.tickChildren()` SHALL still process network packets and player list updates
- **AND** the server SHALL NOT disconnect or time out

### Requirement: Boss music plays through freeze

SoundManager tick SHALL NOT be affected by the freeze. Boss music and ambient sounds SHALL continue normally.

#### Scenario: Sound continues during freeze
- **WHEN** time freeze is active AND a sound event (e.g., boss music) was playing
- **THEN** the sound SHALL continue without interruption
- **AND** new sound events SHALL still play normally
