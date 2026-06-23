# wall-run-transitions Specification

## Purpose
TBD - created by archiving change wall-run-full-rotation. Update Purpose after archive.
## Requirements
### Requirement: Auto corner detection
When the current wall ends (wall normal no longer detects a wall), the system SHALL automatically scan for a new wall in all directions and transition.

#### Scenario: Corner transition to new wall
- **WHEN** player runs along a wall and reaches a corner where an adjacent wall exists
- **THEN** the wall normal SHALL update to the new wall's normal within 1 tick
- **AND** the camera SHALL smoothly SLERP to the new up direction

#### Scenario: No wall found stops wall-run
- **WHEN** player reaches a wall edge and no adjacent wall exists in any direction
- **THEN** wall-run SHALL stop and camera SHALL begin smooth recovery to world up

### Requirement: Wall existence validation
Every tick during wall-run, the system SHALL validate that the current wall normal still has a wall.

#### Scenario: Wall normal recheck on handleWallRunTick
- **WHEN** `handleWallRunTick` runs on the server
- **THEN** it SHALL check `WallUtil.getWallDirection(player, 0.5)` with the current wall normal direction
- **IF** no wall found in current direction, SHALL expand scan to all 4 directions

