# wall-run-physics Specification

## Purpose
TBD - created by archiving change wall-run-full-rotation. Update Purpose after archive.
## Requirements
### Requirement: Wall surface friction check
Wall-run SHALL be prevented on slippery surfaces (ice, packed ice, blue ice).

#### Scenario: Slippery surface blocks wall-run start
- **WHEN** player attempts to start wall-run and the wall block's `getFriction()` >= 0.8
- **THEN** wall-run SHALL NOT start

#### Scenario: Slippery surface stops active wall-run
- **WHEN** player is wall-running and moves onto a wall block with `getFriction()` >= 0.8
- **THEN** wall-run SHALL stop immediately

### Requirement: Gravity aligned with wall surface
The "downward" force during wall-run SHALL be the world gravity vector (-Y) projected onto the wall surface plane, not world -Y directly.

#### Scenario: Vertical gravity on wall
- **WHEN** player is wall-running
- **THEN** the downward drift SHALL be along the wall surface direction closest to world -Y
- **AND** the pull toward wall SHALL remain perpendicular to the surface

### Requirement: Wall-run activation condition
Wall-run SHALL activate when the player is sprinting and near a runnable wall, regardless of being on ground or in air, as long as not in slam state.

#### Scenario: Activate from ground sprint
- **WHEN** player is sprinting on ground and moves close to a runnable wall
- **THEN** wall-run SHALL start (transition to wall surface physics)

#### Scenario: Activate from mid-air
- **WHEN** player is sprinting in air and moves close to a runnable wall
- **THEN** wall-run SHALL start

#### Scenario: No activation during slam
- **WHEN** player is in slam state (`isSlamming == true`)
- **THEN** wall-run SHALL NOT start even if sprinting near wall

