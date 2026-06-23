# vertical-wall-run (implemented)

**Schema:** spec-driven  
**Created:** 2026-06-23  
**Archived:** 2026-06-23  

Vertical wall-running (Titanfall-style wall climb) implemented via client-side
`setDeltaMovement` in `PrototypeRenderEvents.java`. Replaced horizontal-along-wall
movement with vertical climb mechanics.

## Changes (commit 99b7245)

- **Movement direction:** `forwardOnWall` (horizontal) → `wallUp` (vertical)
- **W/S:** climb up / slide down (replacing always-forward)
- **A/D:** strafe left/right on wall
- **Gravity:** projected gravity on wall plane → pull toward wall surface (`wallN * -WALL_GRAVITY`)
- **Pull:** `wallN * WALL_PULL_SPEED` keeps player attached to wall

## Key files

- `PrototypeRenderEvents.java` — client-side `setDeltaMovement` for wall-run velocity

## Config keys

- `WALL_RUN_SPEED` — climb/strafe speed
- `WALL_GRAVITY` — pull strength toward wall surface
- `WALL_PULL_SPEED` — additional attachment force toward wall
