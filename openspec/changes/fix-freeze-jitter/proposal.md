## Why

Time freeze activated when player opens TimeFocusPouch. `ClientLevel.tickEntities()` cancelled, `getPartialTick()` forced to 0. But server keeps sending position packets 鈫?`setPos()` updates `xo` + `x` 鈫?`getPosition(0) = xo = latest network position` 鈫?camera snaps every packet 鈫?visual jitter. Freeze looks broken.

## What Changes

- Add `Entity` mixin overriding `getPosition(float)` during freeze to return frozen snapshots
- On freeze activate: snapshot all loaded entity positions into a map
- On freeze deactivate: clear map, let normal lerp resume
- Handle edge cases: new entities spawned during freeze, entity unload, dimension switch
- Also freeze entity rotation (`yRotO`/`xRotO`) during freeze
- Remove logspam from diagnostics (optional cleanup)

## Capabilities

### New Capabilities

- `render-freeze`: Override `Entity.getPosition(float)` to return frozen position snapshot during freeze. Pure render-layer intercept, no network/physics side effects.

### Modified Capabilities

None.

## Impact

- `src/main/java/com/GoetyMech/addon/mixin/` 鈥?new `EntityMixin.java` targeting `Entity.getPosition(F)`
- `src/main/java/com/GoetyMech/addon/client/TimeFreezeHandler.java` 鈥?add snapshot lifecycle hooks
- `src/main/resources/GoetyMech.mixins.json` 鈥?register new mixin
- No change to network, physics, or server code
