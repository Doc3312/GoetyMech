# wall-run-camera-rotation Specification

## Purpose
TBD - created by archiving change wall-run-full-rotation. Update Purpose after archive.
## Requirements
### Requirement: Camera full rotation during wall-run
When wall-running, the camera SHALL rotate so that its local up vector aligns with the wall normal, making the wall feel like ground.

#### Scenario: Camera up = wall normal on wall-run start
- **WHEN** player starts wall-running (sprinting + near wall in mid-air)
- **THEN** camera up vector rotates from world up (0,1,0) to wall normal over 5-10 ticks via SLERP interpolation

#### Scenario: Camera returns to world up on wall-run stop
- **WHEN** player stops wall-running (wall ends, stops sprinting, or presses shift)
- **THEN** camera up vector SHALL smoothly interpolate back to world up over 10 ticks via SLERP

### Requirement: Smooth camera interpolation
Camera rotation SHALL use SLERP (spherical linear interpolation) to avoid snapping.

#### Scenario: Interpolation uses configurable speed
- **WHEN** camera rotation target changes (start, stop, corner)
- **THEN** the interpolation factor per tick SHALL be controlled by a config value `wall_rotation_speed` (default 0.15)

### Requirement: Third-person model rotation
The player model SHALL rotate to match the camera orientation during wall-run.

#### Scenario: Model rotates in third-person
- **WHEN** player is wall-running and camera is in third-person mode
- **THEN** the player model SHALL have pitch/yaw/roll applied (via `onRenderPlayerPre`) to visually align with the wall surface

