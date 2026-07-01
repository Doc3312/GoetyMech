## 1. Snapshot Manager

- [ ] 1.1 Create `SnapManager.java` in `client` package with `Map<Integer, EntitySnapshot>` field
- [ ] 1.2 Implement `freezeAll(ClientLevel)` to iterate entities and capture position + rotation
- [ ] 1.3 Implement `thawAll()` to clear the map
- [ ] 1.4 Implement `getPosition(int entityId)` returning `Vec3` or null
- [ ] 1.5 Implement `getRotation(int entityId)` returning `Vec2` or null
- [ ] 1.6 Implement `onEntityAdded(Entity)` for lazy snapshot of new entities
- [ ] 1.7 Wire `activate()` → `freezeAll()` and `deactivate()` → `thawAll()` in `TimeFreezeHandler`

## 2. Entity Mixin for Position Freeze

- [ ] 2.1 Create `EntityMixin.java` targeting `Entity.getPosition(F)` (`m_20318_`)
- [ ] 2.2 Inject at `@At("HEAD")` with `cancellable = true`
- [ ] 2.3 If frozen, return `SnapManager.getPosition(id)` instead of original lerp
- [ ] 2.4 Register mixin in `GoetyMech.mixins.json` under `client`

## 3. Entity Rotation Freeze

- [ ] 3.1 Create `EntityRenderStateMixin` or use same `EntityMixin` to freeze yaw/pitch
- [ ] 3.2 Override entity rotation getters during freeze to return captured rotation

## 4. ClientLevelMixin Enhancement

- [ ] 4.1 In `tickEntities()` cancellation block, call `SnapManager.onEntityAdded()` for new entities that were just added to level

## 5. Diagnostics Cleanup (Optional)

- [ ] 5.1 Remove or quiet `FreezeDiagnostics` DRIFT/CAMERA ROTATION logspam
- [ ] 5.2 Remove `GoetyMech$driftCheckCounter` loop from `ClientLevelMixin`

## 6. Verification

- [ ] 6.1 Build and run client
- [ ] 6.2 Test freeze while Creative flying — no camera jitter
- [ ] 6.3 Test freeze with CrushingWheel active — entity positions frozen
- [ ] 6.4 Test deactivate — no snap to wrong position
- [ ] 6.5 Test dimension switch during freeze — map cleared correctly
- [ ] 6.6 Test entity spawned during freeze — gets snapshot on first render
