# 飞行 Mixin — 开发经验

来源：prototype-flight-mixin skill（等待重构合并至统一 physics skill）

## Mixin 注入点选择

| 目标 | 方法 | 适用场景 |
|------|------|---------|
| `LivingEntity.travel()` | `@At("HEAD")` | 需要覆盖所有生物的飞行 |
| `Player.travel()` | `@At("HEAD")` cancellable | 玩家专属，更可控，可完全接管 |

玩家专属功能推荐 `@Mixin(Player.class)` + `cancellable = true`。

## 客户端-服务端同步陷阱

- `player.zza` 只在 `LocalPlayer`（客户端）有效，`ServerPlayer` 总是 0
- `PersistentData` 在两端是独立实例，C2S handler 只写服务端
- **黄金法则**：需要在两端读同一份中间状态时，用本来就同步的字段，或在 C2S 发送端同时写本端

## 调试方法

1. `LOGGER.info` 看 Mixin 是否触发，区分 `[Server thread]` 和 `[Render thread]`
2. 日志中看 forward 值是否 > 0.01
3. Mixin 只在服务端触发 → 客户端 forward 为 0 → C2S 或本端写入问题
