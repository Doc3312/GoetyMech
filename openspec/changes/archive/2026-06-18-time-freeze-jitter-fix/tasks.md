## 1. Mixin Layer Update

- [ ] 1.1 Rewrite `ClientLevelMixin.java`: change injection target from `tick()` to `tickEntities()`, keep `@At("HEAD") cancellable=true`
- [ ] 1.2 Create `MinecraftMixin.java`: `@Inject(method="getPartialTick", HEAD, cancellable=true, remap=false)` returning 0.0F when frozen
- [ ] 1.3 Delete `EntityMixin.java` (move()-based approach, no longer needed)
- [ ] 1.4 Update `goetypunk.mixins.json`: client array = `[ClientLevelMixin, MinecraftMixin, ParticleEngineMixin]`

## 2. Verify

- [ ] 2.1 Run `gradlew build` and confirm zero compilation errors
- [ ] 2.2 Verify mixin count in jar matches expected: 3 client + 1 common
