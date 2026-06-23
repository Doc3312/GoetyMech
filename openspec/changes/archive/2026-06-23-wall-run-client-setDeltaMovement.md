# wall-run-client-setDeltaMovement (superseded)

**Schema:** spec-driven  
**Created:** 2026-06-23  
**Archived:** 2026-06-23  

Client-side `setDeltaMovement` approach for wall-running. Attempted to move the player
by calling `player.setDeltaMovement()` from client tick events.

**Result:** Failed to produce horizontal displacement. Player's position did not change
despite correct move vector computation — see `docs/incidents/2026-06-23-wall-run-no-movement-v2.md`.

**Why superseded:** Design goal was VERTICAL wall-running (Titanfall-style wall climb),
not horizontal wall-running. Client-side setDeltaMovement was the wrong approach.
