# 编译错误积压：Gradle 增量缓存掩盖问题

status: verified

## 问题

项目中有 7 个编译错误，涉及 4 个文件：
- `PrototypeItem.java` — 缺少 `import net.minecraft.world.item.Item`
- `PrototypeEvents.java` — 缺少 `ItemEntity` 导入，重复的 `onLivingAttack` 和 `onLevelTick` 方法定义，无效的 `getEntities().getAll()` API 调用
- `MimicCombinationRecipe.java` — `RecipeSerializer` 需要 `fromJson` 而不是 `codec()`
- `MimicDeconstructionRecipe.java` — 同上
- `PrototypeMimicItem.java` — 缺少 `ModItems` 导入

## 根因

这些错误是之前的 auto-commit 引入的（代码写错了 API），但 Gradle 增量编译只重编译改动过的文件，缓存了旧的 `.class` 文件，导致 BUILD SUCCESSFUL 掩盖了错误。只有当 `--no-daemon` 全量重编译时才暴露。

## 教训

1. 每次 auto-commit 后应验证全量编译是否通过（`gradlew clean compileJava`）
2. `LivingAttackEvent.getEntity()` 返回 `LivingEntity`，不能用 `instanceof ItemEntity`（ItemEntity 不继承 LivingEntity）
3. Forge 47.4.0 的 `RecipeSerializer` 需要 `fromJson(ResourceLocation, JsonObject)`，`codec()` 是 1.20.2+ 的 API
4. `Level.getEntities()` 无参方法在 1.20.1 Forge 中不存在，需用 `getEntitiesOfClass()` 或 `getEntities(EntityTypeTest, AABB, Predicate)`

## 影响

修复后 BUILD SUCCESSFUL，项目恢复正常编译。
