## Context

GoetyPunk time freeze cancels `ClientLevel.tickEntities()` + forces `getPartialTick() = 0`. But network packets (`ClientboundPlayerPositionPacket`, `ClientboundTeleportEntityPacket`) call `Entity.setPos()` → `setPosRaw()` which overwrites `xo`/`yo`/`zo` alongside `x`/`y`/`z`. Since `getPosition(0) = xo`, every server position correction snaps the render position → camera jitter.

Fix must not touch network layer (risk of desync) or `setPos`/collision system (risk of crash). Pure render-layer intercept is safest.

## Goals / Non-Goals

**Goals:**
- Entity render positions frozen during freeze
- Entity rotations frozen during freeze
- Player camera stable during freeze
- Zero impact on network/physics/collision
- Clean transition on deactivate (no snap)

**Non-Goals:**
- Server-side freeze (freeze is client-only by design)
- Block entity / tile entity render freezing (separate concern)
- Sound freezing (separate concern)
- Particle freezing (already handled by `ParticleEngineMixin`)

## Decisions

### Decision 1: Override `Entity.getPosition(float)` at render layer

Over `setPosRaw()` mixin or network packet intercept.

**Why:**
- Render layer intercept has zero side effects on network, physics, or collision
- `setPosRaw()` mixin risks AABB/collision breakage
- Network packet intercept risks desync + server anti-cheat disconnection

**Approach:**
```java
@Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
private void onGetPosition(float pt, CallbackInfoReturnable<Vec3> cir) {
    if (TimeFreezeHandler.isFrozen()) {
        Vec3 snap = SnapManager.get(((Entity)(Object)this).getId());
        if (snap != null) cir.setReturnValue(snap);
    }
}
```

### Decision 2: Separate snapshot manager class

Avoid bloating `TimeFreezeHandler` with map logic.

**Why:** Single responsibility — `TimeFreezeHandler` owns freeze state, `SnapManager` owns position snapshots.

`SnapManager` API:
- `freezeAll(ClientLevel)` — iterate entities, snapshot position + rotation
- `thawAll()` — clear map
- `getPosition(int entityId)` → `Vec3` or null
- `getRotation(int entityId)` → `Vec2` or null
- `onEntityAdded(Entity)` — snapshot new entity

### Decision 3: Snapshot by entity ID (not UUID)

**Why:** Entity ID is stable within a dimension session, faster HashMap lookup than UUID. On dimension switch, map is cleared anyway.

### Decision 4: Include rotation freeze

Capture `entity.yRot` / `entity.xRot` on freeze. Override rotation in the render pipeline via same mixin approach (or capture `yRotO`/`xRotO` field access).

**Why:** Without rotation freeze, entities can still appear to jitter if server sends rotation updates (head turning, etc.).

### Decision 5: No deactivate snap mitigation

**Why:** On deactivate, `getPosition(0) = xo` which has been tracking network updates via `setPos()`. Entity will be at latest server position = where the server thinks it is = correct position. No snap.

**Exception:** If entity was teleported by server during freeze, `xo` = teleport target = abrupt change. Acceptable edge case — teleport during freeze is rare and the snap is the correct gameplay behavior (teleport happened).

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| Memory leak from snapshot map | `thawAll()` on deactivate + dimension switch; use weak entity references or clear on world unload |
| New entity during freeze has no snapshot | `onEntityAdded()` hook in `ClientLevelMixin` or lazy snapshot on first `getPosition()` call |
| Entity ID collision across dimensions | Map keyed by (dimension, id) or clear on dimension switch |
| Rotation freeze inconsistent (yRot vs yRotO) | Always capture `entity.yRot` / `entity.xRot` (current), not `O` variants |

## Migration Plan

1. Create `SnapManager.java` in client package
2. Create `EntityMixin.java` targeting `Entity.getPosition(F)` and rotation
3. Wire `TimeFreezeHandler.activate()` → `SnapManager.freezeAll()`
4. Wire `TimeFreezeHandler.deactivate()` → `SnapManager.thawAll()`
5. Register mixin in `goetypunk.mixins.json`
6. Test in-game with Creative flying + TimeFocusPouch open
7. Clean up `FreezeDiagnostics` log noise (optional)

## Open Questions

None.
