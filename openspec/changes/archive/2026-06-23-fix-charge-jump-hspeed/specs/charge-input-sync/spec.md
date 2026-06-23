## ADDED Requirements

### Requirement: Client sends forward and jumping state in jump charge packets

`C2SJumpChargePacket` SHALL carry three additional fields: `float forward`, `boolean jumping`, `double clientGroundSpeed`.

- `forward`: `player.zza` at the time the packet is sent
- `jumping`: whether the jump key is held at the time the packet is sent
- `clientGroundSpeed`: horizontal speed `sqrt(motion.x^2 + motion.z^2)` computed client-side, only meaningful on the first charge tick (field 0 otherwise)

#### Scenario: Charge starts with forward value

- **WHEN** client begins charging jump (`clientChargeTicks == 1`)
- **THEN** `C2SJumpChargePacket` is sent with `action=1`, `forward=player.zza` (typically 0.98), `jumping=true`, and `clientGroundSpeed` = client's current horizontal speed

#### Scenario: Charge progress includes forward

- **WHEN** client has `clientChargeTicks % 5 == 0` and `clientChargeTicks != lastSentCharge`
- **THEN** `C2SJumpChargePacket` is sent with `action=clientChargeTicks`, `forward=player.zza`, `jumping=true`, and `clientGroundSpeed=0`

#### Scenario: Charge release includes final forward

- **WHEN** client releases the charge (`jumpDown=false` and `lastSentCharge > 0`)
- **THEN** `C2SJumpChargePacket` is sent with `action=0`, `forward=player.zza`, `jumping=false`, and `clientGroundSpeed=0`

### Requirement: Server handler applies setPlayerInput and clientGroundSpeed

`handleJumpChargeFromClient` SHALL accept the new parameters and apply them server-side.

- `player.setPlayerInput(forward, 0, jumping, false)` SHALL be called with the received values
- If `clientGroundSpeed > 0.001`, `PrototypeItem.setGroundSpeed(player, clientGroundSpeed)` SHALL be called
- The existing charge/release logic SHALL remain unchanged

#### Scenario: Input sync runs before charge logic

- **WHEN** `handleJumpChargeFromClient` is called
- **THEN** `player.setPlayerInput` is executed BEFORE any charge logic (ground takeoff check in the same tick sees the correct zza)

#### Scenario: Non-charging zza stays at 0

- **WHEN** player is not currently charging jump (no packet sent)
- **THEN** server `player.zza` remains 0 (vanilla behavior unchanged)

### Requirement: groundSpeed tracking accepts client-reported values

The `onPlayerTick` END phase SHALL continue to track `groundSpeed` via `moved` as a fallback, but the client-reported value (received via `C2SJumpChargePacket` on tick 1) SHALL take precedence.

#### Scenario: Client reports groundSpeed on tick 1

- **WHEN** server receives `C2SJumpChargePacket` with `clientGroundSpeed > 0.001`
- **THEN** `PrototypeItem.getGroundSpeed(player)` returns the client-reported value for the subsequent `applyChargedJump()` call
- **AND** the client-reported value is NOT overwritten by the same tick's `moved`-based tracking (the END phase sees `isChargedJumping=false` and `charge > 0`, so the `groundSpeed` setter in END phase does not run yet)

### Requirement: Diagnostic Mixin removal

`ServerGamePacketListenerImplMixin` SHALL be removed or stripped of all diagnostic `@Inject` methods once input sync is verified working.

#### Scenario: Clean logs

- **WHEN** server runs with the fix applied
- **THEN** no `HANDLE_INPUT_CALLED`, `HANDLE_MOVE_PLAYER called`, or `HANDLE_PLAYER_COMMAND called` spam appears in logs
